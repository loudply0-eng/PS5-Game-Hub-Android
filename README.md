# PS5 Game Hub — Android

An Android game hub and integration layer for the experimental KytyPS5 Android engine.

## Current state

The unified integration is in Draft review:
- The maintained KytyPS5 Android launcher, game library, GPU-driver controls, and SDL3 `GameActivity` are preserved in the project-owned fork.
- The in-app stable-release updater is integrated into that same launcher.
- The production Android application ID is `com.ps5gamehub.android`.
- The ARM64 native engine and FFmpeg compile successfully; the validation APK was independently checked for all six ELF64 AArch64 libraries.
- A production Release workflow now builds a signed APK, verifies the signature against the existing app's SHA-256 certificate fingerprint, and uploads a signed Actions artifact. Branch builds do not publish public releases.

**No production-signed APK has been delivered yet.** The original signing keystore and the installed app's certificate fingerprint must be configured in Actions before the signed build can pass. Device install/update and game compatibility tests also remain outstanding.

## Engine selected

The maintained source fork is [loudply0-eng/KytyPS5-Android](https://github.com/loudply0-eng/KytyPS5-Android), derived from [tarek369/KytyPS5-Android](https://github.com/tarek369/KytyPS5-Android), branch `android-boot-wip`.

Reviewed upstream starting revision: `9ab8b82daf2de1f763491b005e1ed1b28f843bb7`. The unified app/updater work is in Draft PR [#1](https://github.com/loudply0-eng/KytyPS5-Android/pull/1).

The upstream port translates PS5 x86-64 guest code through FEXCore JIT on Android ARM64 and translates graphics through Vulkan, using Mesa Turnip on supported Qualcomm Adreno GPUs. The upstream project is experimental and licensed GPL-2.0-only.

See [docs/ENGINE_INTEGRATION.md](docs/ENGINE_INTEGRATION.md) for the integration architecture, build phases, licensing, security rules, and device-validation gates.

## Device target

Initial target: Realme GT Neo 5 SE (Snapdragon 7+ Gen 2 / Adreno 725).

The upstream project's published reference device is OnePlus Pad 3 (Snapdragon 8 Elite / Adreno 830 / 16 GB RAM). Upstream says other GPUs are untested. **Adreno 725 compatibility and performance are unknown until tested on the actual phone.** A successful APK build does not mean a game is playable.

## Build

The integrated Android module lives at `engine/KytyPS5-Android/android` in the parent build workspace. The reproducible workflow uses Java 21, Android SDK 36, NDK 28.2, CMake, Ninja, glslangValidator, and the pinned FFmpeg build script. The native validation workflow is not the user-facing release build.

The signed production workflow builds Release, checks every native library and the original signing certificate, and uploads the APK artifact. Its source branch build does not create a public release. See [docs/PRODUCTION_RELEASE_SETUP.md](docs/PRODUCTION_RELEASE_SETUP.md) for the five required Actions secrets and exact setup instructions. The in-app updater checks the latest stable GitHub Release on startup and supports manual checks. Android always asks the user to approve installation.

## Remaining work

1. Configure the original keystore and installed-app certificate fingerprint as GitHub Actions secrets.
2. Complete the signed Release build and verify its certificate matches exactly.
3. Install over the existing app and test an in-app update on a physical Android device.
4. Test actual owned-game compatibility on Adreno 725; publish only measured per-game results.
5. Complete PR review/merge and authorize a protected stable release tag.

## Legal and security

Use only game dumps you own. This project does not include games, saves, keys, firmware, or proprietary system software. Preserve GPL-2.0-only and third-party notices and meet the applicable source-distribution obligations when distributing derived binaries. Never commit game dumps, firmware, console keys, or signing secrets.
