# PS5 Game Hub — Android

An Android game hub and integration layer for the experimental KytyPS5 Android engine.

## Current state

This repository currently contains a Kotlin launcher shell only:
- Dark Android launcher UI and Storage Access Framework game-folder picker.
- GitHub Releases update check for this hub.
- Game launching remains disabled until the native engine is integrated and validated.

**This repository is not yet a working PS5 emulator and cannot currently launch PS5 games.** The native KytyPS5 engine, FEXCore JIT, Vulkan/Turnip runtime, native libraries, controller path, and target-device validation are not yet integrated here.

## Engine selected

The selected upstream engine is [tarek369/KytyPS5-Android](https://github.com/tarek369/KytyPS5-Android), branch `android-boot-wip`.

Reviewed upstream revision: `9ab8b82daf2de1f763491b005e1ed1b28f843bb7`.

The upstream port translates PS5 x86-64 guest code through FEXCore JIT on Android ARM64 and translates graphics through Vulkan, using Mesa Turnip on supported Qualcomm Adreno GPUs. The upstream project is experimental and licensed GPL-2.0-only.

See [docs/ENGINE_INTEGRATION.md](docs/ENGINE_INTEGRATION.md) for the integration architecture, build phases, licensing, security rules, and device-validation gates.

## Device target

Initial target: Realme GT Neo 5 SE (Snapdragon 7+ Gen 2 / Adreno 725).

The upstream project's published reference device is OnePlus Pad 3 (Snapdragon 8 Elite / Adreno 830 / 16 GB RAM). Upstream says other GPUs are untested. **Adreno 725 compatibility and performance are unknown until tested on the actual phone.** A successful APK build does not mean a game is playable.

## Build

The current launcher shell can be opened in Android Studio with JDK 17 or newer and Android SDK Platform 35. This builds only the shell, not the PS5 engine.

The upstream native build has a separate documented toolchain, including Android NDK 28.2, CMake, Ninja, glslangValidator, and Gradle. Follow the upstream [Android contributor guide](https://github.com/tarek369/KytyPS5-Android/blob/android-boot-wip/docs/android/CONTRIBUTING.md); do not treat this shell build as an emulator build.

## Planned work

1. Audit and pin upstream source, submodules, and license notices.
2. Reproduce the upstream ARM64 native build.
3. Package the exact native libraries, SDL glue, driver assets, and launcher contract.
4. Integrate hub UX without breaking engine-critical components.
5. Add CI artifacts and safe release updates.
6. Test on Adreno 725 and publish per-game compatibility results.

## Legal and security

Use only game dumps you own. This project does not include games, saves, keys, firmware, or proprietary system software. Preserve GPL-2.0-only and third-party notices and meet the applicable source-distribution obligations when distributing derived binaries. Never commit game dumps, firmware, console keys, or signing secrets.
