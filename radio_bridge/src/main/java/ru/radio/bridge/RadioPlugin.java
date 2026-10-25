package ru.radio.bridge;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.LocationalSoundPacketEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Radio channels for Simple Voice Chat.
 * A player's channel is the scoreboard score "radio.channel" (written by the datapack, 0 = off).
 * 1) While a player speaks and has channel > 0, their (filtered) voice goes to everyone else on the same channel.
 * 2) Music from Audio Player (custom discs) played by a jukebox next to an active station is forwarded to the station's channel.
 * Normal proximity voice is not touched.
 */
public class RadioPlugin implements VoicechatPlugin {
    private static final Logger LOG = LoggerFactory.getLogger("radio_bridge");
    private static final long SESSION_TIMEOUT_MS = 600;
    private static final int STATION_REFRESH_EVERY = 4; // x 250 ms

    private volatile VoicechatServerApi api;
    private volatile RadioConfig config = RadioConfig.defaults();
    private volatile Snapshot snapshot = Snapshot.EMPTY;
    private volatile MinecraftServer server;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, MusicStream> music = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;
    private List<Station> cachedStations = List.of(); // main thread only
    private int refreshCounter;                        // main thread only

    @Override
    public String getPluginId() {
        return "radio_bridge";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onStarted);
        registration.registerEvent(VoicechatServerStoppedEvent.class, this::onStopped);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
        registration.registerEvent(LocationalSoundPacketEvent.class, this::onLocational);
    }

    private void onStarted(VoicechatServerStartedEvent event) {
        api = event.getVoicechat();
        config = RadioConfig.load(FabricLoader.getInstance().getConfigDir().resolve("radio_bridge.properties"));
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "radio-bridge");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::housekeeping, 1000, 250, TimeUnit.MILLISECONDS);
        LOG.info("Radio Bridge started (objective '{}')", config.objective);
    }

    private void onStopped(VoicechatServerStoppedEvent event) {
        if (scheduler != null) scheduler.shutdownNow();
        sessions.values().forEach(Session::close);
        sessions.clear();
        music.values().forEach(MusicStream::close);
        music.clear();
        snapshot = Snapshot.EMPTY;
        server = null;
    }

    // ---------------------------------------------------------------- housekeeping (scheduler thread)

    private void housekeeping() {
        try {
            MinecraftServer srv = server;
            if (srv != null) {
                srv.execute(() -> refreshSnapshot(srv)); // scoreboard is read on the main thread
            }
            long now = System.currentTimeMillis();
            sessions.entrySet().removeIf(e -> {
                if (now - e.getValue().lastPacket > SESSION_TIMEOUT_MS) {
                    e.getValue().close();
                    return true;
                }
                return false;
            });
            music.entrySet().removeIf(e -> {
                if (now - e.getValue().lastPacket > SESSION_TIMEOUT_MS) {
                    e.getValue().close();
                    return true;
                }
                return false;
            });
        } catch (Throwable t) {
            LOG.warn("Radio Bridge housekeeping failed", t);
        }
    }

    // ---------------------------------------------------------------- main thread

    private void refreshSnapshot(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(config.objective);
        Map<UUID, Integer> channelOf = new HashMap<>();
        Map<Integer, List<UUID>> members = new HashMap<>();
        Map<UUID, PlayerInfo> info = new HashMap<>();
        if (objective != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(player, objective);
                int channel = score == null ? 0 : score.value();
                if (channel > 0) {
                    UUID id = player.getUUID();
                    channelOf.put(id, channel);
                    members.computeIfAbsent(channel, k -> new ArrayList<>()).add(id);
                    info.put(id, new PlayerInfo(player.level(), player.getX(), player.getY(), player.getZ()));
                }
            }
        }
        if (config.captureAudioplayer && refreshCounter++ % STATION_REFRESH_EVERY == 0) {
            cachedStations = findStations(server, scoreboard);
        }
        snapshot = new Snapshot(channelOf, members, info, config.captureAudioplayer ? cachedStations : List.of());
    }

    /** Active radio stations = markers tagged radio.station with radio.active = 1 (maintained by the datapack). */
    private static List<Station> findStations(MinecraftServer server, Scoreboard scoreboard) {
        Objective freq = scoreboard.getObjective("radio.freq");
        Objective active = scoreboard.getObjective("radio.active");
        if (freq == null || active == null) return List.of();
        List<Station> result = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            List<? extends Marker> markers = level.getEntities(EntityTypeTest.forClass(Marker.class),
                    m -> m.getTags().contains("radio.station"));
            for (Marker m : markers) {
                ReadOnlyScoreInfo a = scoreboard.getPlayerScoreInfo(m, active);
                ReadOnlyScoreInfo f = scoreboard.getPlayerScoreInfo(m, freq);
                if (a != null && a.value() == 1 && f != null && f.value() > 0) {
                    result.add(new Station(level, m.getX(), m.getY(), m.getZ(), f.value()));
                }
            }
        }
        return result;
    }

    // ---------------------------------------------------------------- voice thread: players' voice

    private void onMicrophone(MicrophonePacketEvent event) {
        VoicechatServerApi voicechat = api;
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (voicechat == null || senderConnection == null) return;

        if (server == null && senderConnection.getPlayer().getPlayer() instanceof ServerPlayer sp) {
            server = sp.level().getServer(); // works on dedicated servers and in singleplayer
        }
        UUID sender = senderConnection.getPlayer().getUuid();
        byte[] opus = event.getPacket().getOpusEncodedData();
        Integer channel = snapshot.channelOf().get(sender);

        // End of speech (empty packet) or radio is off: close the session
        if (channel == null || opus == null || opus.length == 0) {
            Session old = sessions.remove(sender);
            if (old != null) old.close();
            return;
        }

        List<UUID> targets = snapshot.members().getOrDefault(channel, List.of());
        if (targets.size() < 2 && !config.loopback) return;

        Session session = sessions.computeIfAbsent(sender, id -> new Session(voicechat, config, id));
        try {
            session.relay(opus, targets, config.loopback);
        } catch (Throwable t) {
            LOG.warn("Radio relay failed", t);
        }
    }

    // ---------------------------------------------------------------- voice thread: Audio Player music

    private void onLocational(LocationalSoundPacketEvent event) {
        VoicechatServerApi voicechat = api;
        if (voicechat == null || !config.captureAudioplayer) return;
        if (!SoundPacketEvent.SOURCE_PLUGIN.equals(event.getSource())) return;
        VoicechatConnection receiver = event.getReceiverConnection();
        if (receiver == null) return;
        Snapshot snap = snapshot;
        if (snap.stations().isEmpty()) return;

        LocationalSoundPacket packet = event.getPacket();
        UUID channelId = packet.getChannelId();
        long seq = packet.getSequenceNumber();

        // The same packet is delivered once per receiver in range: forward it only once
        MusicStream known = music.get(channelId);
        if (known != null && seq <= known.lastSeq) return;

        Object level = receiver.getPlayer().getServerLevel().getServerLevel();
        Position pos = packet.getPosition();
        double maxSq = config.captureRadius * config.captureRadius;
        Station best = null;
        double bestSq = maxSq;
        for (Station s : snap.stations()) {
            if (s.level() != level) continue;
            double dx = s.x() - pos.getX(), dy = s.y() - pos.getY(), dz = s.z() - pos.getZ();
            double d = dx * dx + dy * dy + dz * dz;
            if (d <= bestSq) {
                best = s;
                bestSq = d;
            }
        }
        if (best == null) return;

        // Players who are in hearing range of the jukebox already hear it: skip them
        double hear = packet.getDistance();
        List<UUID> targets = new ArrayList<>();
        for (UUID id : snap.members().getOrDefault(best.freq(), List.of())) {
            PlayerInfo pi = snap.info().get(id);
            if (pi != null && pi.level() == level) {
                double dx = pi.x() - pos.getX(), dy = pi.y() - pos.getY(), dz = pi.z() - pos.getZ();
                if (dx * dx + dy * dy + dz * dz <= hear * hear) continue;
            }
            targets.add(id);
        }

        MusicStream stream = music.computeIfAbsent(channelId, id -> new MusicStream(voicechat, id));
        try {
            stream.send(packet.getOpusEncodedData(), seq, targets);
        } catch (Throwable t) {
            LOG.warn("Radio music relay failed", t);
        }
    }

    // ---------------------------------------------------------------- types

    private record Station(Object level, double x, double y, double z, int freq) { }

    private record PlayerInfo(Object level, double x, double y, double z) { }

    private record Snapshot(Map<UUID, Integer> channelOf, Map<Integer, List<UUID>> members,
                            Map<UUID, PlayerInfo> info, List<Station> stations) {
        static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of(), Map.of(), List.of());
    }

    /** Sends one opus stream to many players as non-positional audio. */
    private static final class Fanout {
        private final VoicechatServerApi api;
        private final UUID streamId;
        private final Map<UUID, StaticAudioChannel> channels = new HashMap<>();

        Fanout(VoicechatServerApi api, UUID streamId) {
            this.api = api;
            this.streamId = streamId;
        }

        synchronized void send(byte[] opus, Collection<UUID> targets, UUID exclude) {
            for (UUID target : targets) {
                if (target.equals(exclude)) continue;
                StaticAudioChannel channel = channels.get(target);
                if (channel == null) {
                    VoicechatConnection connection = api.getConnectionOf(target);
                    if (connection == null || !connection.isInstalled()) continue;
                    channel = api.createStaticAudioChannel(streamId, connection.getPlayer().getServerLevel(), connection);
                    if (channel == null) continue;
                    channels.put(target, channel);
                }
                channel.send(opus);
            }
        }

        synchronized void close() {
            for (StaticAudioChannel c : channels.values()) c.flush();
            channels.clear();
        }
    }

    /** One continuous transmission of one player. */
    private static final class Session {
        private final UUID sender;
        private final OpusDecoder decoder;
        private final OpusEncoder encoder;
        private final RadioFilter filter;
        private final Fanout fanout;
        volatile long lastPacket = System.currentTimeMillis();
        private boolean closed;

        Session(VoicechatServerApi api, RadioConfig cfg, UUID sender) {
            this.sender = sender;
            this.decoder = api.createDecoder();
            this.encoder = api.createEncoder();
            this.filter = new RadioFilter(cfg);
            this.fanout = new Fanout(api, UUID.nameUUIDFromBytes(("radio:" + sender).getBytes(StandardCharsets.UTF_8)));
        }

        synchronized void relay(byte[] opus, List<UUID> targets, boolean loopback) {
            if (closed) return;
            lastPacket = System.currentTimeMillis();
            short[] pcm = decoder.decode(opus);
            filter.process(pcm);
            byte[] out = encoder.encode(pcm);
            fanout.send(out, targets, loopback ? null : sender);
        }

        synchronized void close() {
            if (closed) return;
            closed = true;
            fanout.close();
            encoder.close();
            decoder.close();
        }
    }

    /** Music forwarded from an Audio Player sound channel (opus passes through unchanged). */
    private static final class MusicStream {
        private final Fanout fanout;
        volatile long lastPacket = System.currentTimeMillis();
        volatile long lastSeq = -1;

        MusicStream(VoicechatServerApi api, UUID sourceChannel) {
            this.fanout = new Fanout(api, UUID.nameUUIDFromBytes(("radio-music:" + sourceChannel).getBytes(StandardCharsets.UTF_8)));
        }

        void send(byte[] opus, long seq, List<UUID> targets) {
            lastPacket = System.currentTimeMillis();
            lastSeq = seq;
            fanout.send(opus, targets, null);
        }

        void close() {
            fanout.close();
        }
    }
}
