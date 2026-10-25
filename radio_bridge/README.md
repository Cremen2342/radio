# Radio Bridge (Fabric 1.21.10, server-side)

Requires: Simple Voice Chat (server), Fabric Loader. Fabric API is NOT needed.

Build: install JDK 21 + Gradle, then in this folder:
    gradle wrapper
    ./gradlew build
Jar: build/libs/radio-bridge-1.0.0.jar -> server `mods/`.
If Gradle can't resolve versions, update loom / loader / voicechat_api_version
(fabricmc.net/develop, maven.maxhenkel.de).

Config: config/radio_bridge.properties (created on first start).

Features
- Voice: players with scoreboard radio.channel > 0 hear each other (light radio filter).
- Audio Player music: custom discs played by a jukebox NEXT TO an active station (within capture_radius)
  are forwarded to the station's channel. Needs at least one player in hearing range of the jukebox,
  because Audio Player only sends audio to players in range.
- Config keys: noise, highpass_hz, lowpass_hz, drive, gain, capture_audioplayer, capture_radius, loopback.
