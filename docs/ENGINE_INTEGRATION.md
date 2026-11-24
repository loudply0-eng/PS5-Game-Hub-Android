# KytyPS5 Android engine integration plan

## Decision

Use the upstream KytyPS5 Android port as the emulator engine and keep this repository as the PS5 Game Hub product/integration layer. Do not describe this launcher as an emulator until the native engine is built, packaged, launched, and tested on real hardware.

## Upstream source pin

- Repository: https://github.com/tarek369/KytyPS5-Android
- Branch reviewed: `android-boot-wip`
- Reviewed commit: `9ab8b82daf2de1f763491b005e1ed1b28f843bb7`
- License: GPL-2.0-only (retain notices and satisfy source/distribution obligations).
- The upstream project includes multiple Git submodules. Any checkout/build pipeline must initialize and pin them; a shallow copy of only the top-level files is not sufficient.

The pin is a reproducible starting point, not a promise that upstream will remain on this commit. Bump it only after reviewing upstream changes and rebuilding.

## Engine architecture

- PS5 x86-64 guest code: FEXCore JIT on Android ARM64.
- Graphics: Vulkan translation with Mesa Turnip for supported Qualcomm Adreno GPUs.
- Android native runtime: Kyty native library plus SDL3 and libadrenotools hooks.
- Android app: upstream launcher/game activity and its JNI/native loading contract.

The upstream Android build guide currently requires Android NDK 28.2, CMake, Ninja, glslangValidator, and JDK/Gradle. It builds the native `kyty_emulator` target and then packages native libraries into the Android app. Follow the upstream contributor guide rather than inventing a parallel native build.

## Target-device compatibility gate

The intended first test device is Realme GT Neo 5 SE (Snapdragon 7+ Gen 2, Adreno 725). Upstream's published reference device is OnePlus Pad 3 (Snapdragon 8 Elite, Adreno 830, 16 GB RAM); upstream states other GPUs are untested. Therefore:

- Adreno 725 compatibility is **unknown** until a native build is installed and tested.
- Do not claim a game is playable based only on APK build success or launcher appearance.
- Collect Vulkan/Turnip initialization logs, native crash logs, memory use, thermals, and per-game FPS.
- Start with a small title already reported by upstream as playable; test larger games only after stability is established.
- Do not promise Astro Bot or demanding UE5 titles will run acceptably on this phone.

## Integration phases

1. **Source and license audit** — review upstream commit, submodules, licenses, build scripts, and app package identity.
2. **Reproducible native build** — build ARM64 dependencies and `libmain.so` using the documented NDK/CMake/Ninja flow.
3. **APK packaging** — stage the exact required ARM64 libraries and assets; preserve upstream launcher activity, SDL Java glue, driver setup, and intent/argument contract.
4. **Hub integration** — merge the hub branding and safe library UX without replacing engine-critical Android components.
5. **CI** — build/test on GitHub Actions with pinned dependencies; publish unsigned CI artifacts until release signing is configured. Never store signing keys, game dumps, firmware, or console keys in the repository.
6. **Device validation** — test on the actual Adreno 725 phone and publish a truthful compatibility report. Keep game launch disabled on devices/builds that fail required engine checks.

## Security and legal rules

- Accept only user-owned game dumps. Never bundle or commit game images, firmware, console keys, or proprietary Sony files.
- Treat game-folder URIs as user-selected content; persist only the minimum read permission needed.
- Verify downloaded driver archives with pinned SHA-256 checksums.
- Use HTTPS for update metadata and downloads; validate release assets before presenting them.
- Keep release signing material in GitHub Actions secrets or an offline secure location, never in Git history or APK source.
- Preserve GPL-2.0-only and third-party license notices. If distributing a combined/derived APK, provide the corresponding source and notices required by the applicable licenses.

## Current status

This repository's existing Kotlin app is a launcher shell. It selects a folder and checks this repository's GitHub Releases, but it does not include KytyPS5 native code, FEXCore, Turnip integration, or a working game-launch path. The next engineering milestone is a reproducible upstream native build, not a cosmetic UI change.
