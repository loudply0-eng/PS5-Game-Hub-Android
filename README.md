# PS5 Game Hub — Android

An Android game hub and integration layer for the experimental KytyPS5 Android engine.

## Current state

The unified integration is under review and CI validation:
- The maintained KytyPS5 Android launcher, game library, GPU-driver controls, and SDL3 `GameActivity` are preserved in the project-owned fork.
- The verified in-app updater is integrated into the same launcher.
- The production Android application ID is `com.ps5gamehub.android`, matching the existing Hub package.
- Release builds require the existing production signing identity; debug builds are for validation only.
- The parent native workflow now builds FFmpeg and the ARM64 KytyPS5 engine, then packages the unified Android app.

**This is not yet a validated game-playing release.** Native compilation, final APK inspection, production certificate matching, installation/update testing, and game compatibility testing must all pass before release.

## Engine selected

The maintained source fork is [loudply0-eng/KytyPS5-Android](https://github.com/loudply0-eng/KytyPS5-Android), derived from [tarek369/KytyPS5-Android](https://github.com/tarek369/KytyPS5-Android), branch `android-boot-wip`.

Reviewed upstream starting revision: `9ab8b82daf2de1f763491b005e1ed1b28f843bb7`. The unified app/updater work is in Draft PR [#1](https://github.com/loudply0-eng/KytyPS5-Android/pull/1).

The upstream port translates PS5 x86-64 guest code through FEXCore JIT on Android ARM64 and translates graphics through Vulkan, using Mesa Turnip on supported Qualcomm Adreno GPUs. The upstream project is experimental and licensed GPL-2.0-only.

See [docs/ENGINE_INTEGRATION.md](docs/ENGINE_INTEGRATION.md) for the integration architecture, build phases, licensing, security rules, and device-validation gates.

## Device target

Initial target: Realme GT Neo 5 SE (Snapdragon 7+ Gen 2 / Adreno 725).

The upstream project's published reference device is OnePlus Pad 3 (Snapdragon 8 Elite / Adreno 830 / 16 GB RAM). Upstream says other GPUs are untested. **Adreno 725 compatibility and performance are unknown until tested on the actual phone.** A successful APK build does not mean a game is playable.

## Build

The integrated Android module lives at `engine/KytyPS5-Android/android` in the parent build workspace. The reproducible CI workflow uses Java 21, Android SDK 36, NDK 28.2, CMake, Ninja, glslangValidator, and the pinned FFmpeg build script. The CI artifact is an internal validation APK, not a production-signed release.

The in-app updater checks stable releases from this repository's GitHub Releases. Android still requires the user to approve installation. See [docs/UNIFIED_KYTYPS5_UPDATER_INTEGRATION.md](docs/UNIFIED_KYTYPS5_UPDATER_INTEGRATION.md) for release gates and current limitations.

## Remaining work

1. Finish and inspect unified updater CI and native ARM64 build runs.
2. Advance the parent repository's submodule gitlink to the exact reviewed fork commit.
3. Verify the release APK certificate against the existing Hub production certificate.
4. Test installation over the existing app and an in-app update on a physical Android device.
5. Test real game compatibility on Adreno 725; publish only measured per-game results.

## Legal and security

Use only game dumps you own. This project does not include games, saves, keys, firmware, or proprietary system software. Preserve GPL-2.0-only and third-party notices and meet the applicable source-distribution obligations when distributing derived binaries. Never commit game dumps, firmware, console keys, or signing secrets.
