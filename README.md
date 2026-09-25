# Dirge Bridge

Dirge Bridge is an independent Android compatibility bridge for running a user's own legally obtained copy of **Final Fantasy VII: Dirge of Cerberus – Lost Episode** on modern Android hardware.

**Current repository baseline:** v0.2.4  
**Package:** `com.wakka.dirgebridge`

## Current status

The v0.2.4 line has been device-tested on a Samsung Galaxy S25 Ultra / Android 16 with working rendering, touch controls, English text support, music/voice/SFX playback, checkpoint persistence support, pause-synchronized audio, field-camera control, and native horizontal weapon aiming.

This repository contains the bridge source and build-support material only. **No Lost Episode game files are included.**

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
