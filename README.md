# PS5 Game Hub — Android

An independent Android launcher shell for experimenting with the KytyPS5 Android port.

## Current state
This repository currently contains an Android application shell only:
- Kotlin Android app and dark launcher screen
- Android Storage Access Framework folder picker
- Clear engine status; game launching remains disabled until native integration is implemented

**This is not yet a PS5 emulator and cannot launch PS5 games.** The native KytyPS5 Android engine, FEXCore JIT, Vulkan/Turnip driver integration, controller/input layer and device validation are not included in this starter.

## Device target
Initial target: Realme GT Neo 5 SE, Snapdragon 7+ Gen 2 / Adreno 725 / 16 GB RAM.
The upstream Android port reports testing on Snapdragon 8 Elite / Adreno 830. Other GPUs are untested upstream, so compatibility and performance on this phone are unknown.

## Build
Open this project in Android Studio with JDK 17 or newer. Install Android SDK Platform 35 and allow Gradle sync, then build the `app` debug variant. A compatible Android SDK/Gradle environment is required.

## Planned work
1. Audit and pin the upstream KytyPS5 Android source revision and its submodules.
2. Follow its Android contributor guide exactly (including NDK 28.2, CMake, Ninja and Gradle).
3. Build ARM64 native dependencies and integrate only through documented JNI/native entry points.
4. Add compatibility checks, safe game-folder selection, controller UI and diagnostics.
5. Test on the actual target phone before claiming any game works.

## Legal and licensing
Use only game dumps you own. This project does not include games, keys, firmware or proprietary system software. The upstream KytyPS5 Android port is GPL-2.0-only; any derived code and bundled third-party components must retain their applicable licenses. Review license obligations before distributing a combined APK.

## Upstream engine
The Android engine is developed separately at [tarek369/KytyPS5-Android](https://github.com/tarek369/KytyPS5-Android), branch `android-boot-wip`. Its contributor guide and GPL-2.0-only terms must be followed before integrating or redistributing engine code.
