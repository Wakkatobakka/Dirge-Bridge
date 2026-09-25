# Dirge Bridge

Dirge Bridge is an independent Android compatibility bridge for running a user's own legally obtained copy of **Final Fantasy VII: Dirge of Cerberus – Lost Episode** on modern Android hardware.

**Current repository baseline:** v0.2.4  
**Package:** `com.wakka.dirgebridge`

## Current status

The v0.2.4 line has been device-tested on a Samsung Galaxy S25 Ultra / Android 16 with working rendering, touch controls, English text support, music/voice/SFX playback, checkpoint persistence support, pause-synchronized audio, field-camera control, and native horizontal weapon aiming.

This repository contains the bridge source and build-support material only. **No Lost Episode game files are included.**

## Why Dirge Bridge exists

Several weeks before this project started, I read about Yuvi’s work recovering *Final Fantasy VII: Dirge of Cerberus – Lost Episode* from an old SD card.

I thought the recovery itself was incredible. I also looked at the process required to actually play the game and thought, basically, “that is way more complicated than I would want it to be.”

Then I forgot about it for a bit.

A few weeks later, I was thinking about wanting to play *Blue Dragon* on my phone and wondering if anyone had used AI to do something like that yet. That made me remember *Lost Episode* — and that I had access to AI.

So I tried it.

I’m not a professional developer. I like Vincent, I think old games are cool, and I enjoy messing around with technology and seeing what I can make work. That was about the full extent of the grand plan.

Roughly twenty hours after starting, *Lost Episode* was running on my modern Android phone with graphics, audio, English translation support, touchscreen controls, and a launcher that made the whole thing feel like an actual usable application instead of a technical experiment.

That became Dirge Bridge.

None of this replaces the recovery, preservation, translation, or research work that came before it. Without that work, there would have been nothing for me to build around. Dirge Bridge is simply my attempt to make the last step much easier: give it your own compatible game data, install the app, and play.

## Quick start

1. Open the **Releases** section and download the latest `Dirge_Bridge_*.apk`.
2. Install the APK on your Android device.
3. Open Dirge Bridge.
4. Choose **Import English game data ZIP** and select your own legally obtained, compatible Lost Episode data package.
5. After the import is verified, choose **Enter Lost Episode**.

Dirge Bridge does not include the Lost Episode game files. Users must supply their own compatible game data.

The current release has been tested on a Samsung Galaxy S25 Ultra running Android 16. Compatibility with other Android devices and Android versions is not yet fully documented.

## Preparing the game-data ZIP

Dirge Bridge does not include Lost Episode game data.

If your compatible English data set is missing `game.dex`, download **Dirge Bridge Payload Builder v0.1.0** from the v0.2.4 release assets.

The builder works locally from your own verified English-patched `game.jar`, `game.jam`, `game.sp`, and the 15 SD `PACK*.JAR` files. It generates `game.dex`, verifies all 19 files against the Dirge Bridge v0.2.4 importer allowlist, and creates the ZIP that Dirge Bridge can import.

On Windows:

1. Extract the Payload Builder ZIP.
2. Double-click `RUN-PAYLOAD-BUILDER-WINDOWS.bat`.
3. Select the ZIPs or folders containing your compatible Lost Episode data.
4. Choose where to save the finished package.
5. Import the generated `Dirge_Lost_Episode_English_Data_for_Dirge_Bridge_v0.2.4.zip` in Dirge Bridge.

Android SDK Platform 35 and Android SDK Build-Tools 35.0.0 are required. Android Studio's SDK Manager is the easiest way to install them.

The builder does not upload your files, alter your originals, or include any Lost Episode game data itself.

## Important boundaries

Do not commit or upload:

- `game.jar`, `game.dex`, `game.jam`, or `game.sp`
- `PACK*.JAR` / SD game data
- private game-data ZIPs
- signing keystores or passwords
- private test recordings or reports unless deliberately reviewed for publication

The included `.gitignore` blocks the common forms of those files, but it is still worth checking before every public release.

## Repository layout

- `src/` — Android bridge/runtime source
- `res/` — Android resources and Dirge Bridge/Wakkan visual assets
- `assets/` — expected payload metadata used by the importer
- `compile-only/` — compile-time compatibility stubs
- `tests/` — regression tests
- `tools/` — build and DEX patch utilities
- `verification/` — selected v0.2.4 verification records
- `docs/` — preservation and release notes

## Translation attribution

The currently supported English data set is based on the Lost Episode English translation work credited in the app to:

- Translation: TurquoiseHammer
- Hacking: Yuvi
- Playtesting: Yuvi, TurquoiseHammer, Odysseus
- Special thanks: Shinra Archaeology

Dirge Bridge is an independent compatibility project and is not affiliated with Square Enix or NTT DOCOMO.

## Building

See [`docs/BUILDING.md`](docs/BUILDING.md).

## License

No repository-wide license has been selected yet. Third-party material retains its own notices; see `THIRD-PARTY-NOTICES.txt`.
