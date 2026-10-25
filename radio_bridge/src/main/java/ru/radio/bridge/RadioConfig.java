package ru.radio.bridge;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Simple key=value config. Created with defaults on first start. */
public final class RadioConfig {
    public final String objective;
    public final double noise;       // white noise amplitude, 0..1 of full scale (0.008 is barely audible hiss)
    public final double highpassHz;  // cuts rumble / low end
    public final double lowpassHz;   // cuts top end (radio "band")
    public final double drive;       // soft clipping amount (1.0 = almost clean)
    public final double gain;        // output gain after filtering
    public final boolean captureAudioplayer; // forward AudioPlayer music from jukeboxes next to a station
    public final double captureRadius;       // max distance (blocks) from station to the sound source
    public final boolean loopback;   // TEST ONLY: you also hear your own radio voice (lets you test alone)

    private RadioConfig(Properties p) {
        objective  = p.getProperty("objective", "radio.channel").trim();
        noise      = clamp(num(p, "noise", 0.008), 0, 0.2);
        highpassHz = clamp(num(p, "highpass_hz", 300), 20, 2000);
        lowpassHz  = clamp(num(p, "lowpass_hz", 3800), 1000, 20000);
        drive      = clamp(num(p, "drive", 1.3), 0.5, 5);
        gain       = clamp(num(p, "gain", 1.0), 0.1, 3);
        captureAudioplayer = Boolean.parseBoolean(p.getProperty("capture_audioplayer", "true").trim());
        captureRadius = clamp(num(p, "capture_radius", 4.0), 1, 32);
        loopback   = Boolean.parseBoolean(p.getProperty("loopback", "false").trim());
    }

    private static double num(Properties p, String k, double def) {
        try { return Double.parseDouble(p.getProperty(k, String.valueOf(def)).trim()); }
        catch (NumberFormatException e) { return def; }
    }
    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }

    public static RadioConfig defaults() { return new RadioConfig(new Properties()); }

    public static RadioConfig load(Path file) {
        Properties p = new Properties();
        try {
            if (Files.exists(file)) {
                try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { p.load(r); }
            } else {
                Files.createDirectories(file.getParent());
                Files.writeString(file, String.join("\n",
                    "# Radio Bridge config",
                    "# Scoreboard objective that holds the player's radio channel (0 = no radio)",
                    "objective=radio.channel",
                    "# White noise level (0 = off). 0.008 is a light hiss, 0.03 is heavy",
                    "noise=0.008",
                    "# Band limits in Hz. Narrower band = more 'radio' sound",
                    "highpass_hz=300",
                    "lowpass_hz=3800",
                    "# Soft clipping (1.0 almost clean, 3.0 crunchy)",
                    "drive=1.3",
                    "# Output gain",
                    "gain=1.0",
                    "# Forward Audio Player (custom discs) music from a jukebox next to an active station to its channel",
                    "capture_audioplayer=true",
                    "# Max distance from the station to the sound source, in blocks",
                    "capture_radius=4.0",
                    "# TEST ONLY: hear your own radio voice (for solo testing). Keep false on a real server",
                    "loopback=false", ""), StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) { }
        return new RadioConfig(p);
    }
}
