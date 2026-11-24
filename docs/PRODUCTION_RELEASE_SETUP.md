# Production Android build and release

The production workflow builds the full KytyPS5 ARM64 engine, packages the existing launcher and the secure in-app updater, signs the APK, checks the signing identity against the existing installed app, and uploads a signed APK artifact. A branch build does **not** publish a public release. A version tag (or an explicitly authorized manual publish from `main`) is required to publish GitHub Releases, which is the updater's stable update source.

## Required Actions secrets

In the repository, open **Settings → Secrets and variables → Actions → New repository secret** and configure all five values:

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Base64 encoding of the original production keystore file (JKS or PKCS12) |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Original production signing key alias |
| `ANDROID_KEY_PASSWORD` | Original signing key password |
| `ANDROID_SIGNING_CERT_SHA256` | SHA-256 certificate fingerprint of the already-installed official app, 64 hex characters (colons accepted) |

**Do not create a replacement keystore.** A new certificate cannot update an existing installation signed by a different certificate. Never paste the keystore or passwords into a chat, issue, commit, workflow log, or repository file.

### Prepare the original certificate fingerprint on Windows

Use the exact APK currently installed on the phone. With USB debugging enabled and the phone connected, run:

```powershell
$apkPath = (adb shell pm path com.ps5gamehub.android | Select-Object -First 1) -replace '^package:', ''
adb pull $apkPath .\existing-hub.apk
```

Find Android SDK Build Tools `apksigner.bat` and inspect the APK certificate:

```powershell
$apksigner = Get-ChildItem "$env:LOCALAPPDATA\Android\Sdk\build-tools\*\apksigner.bat" | Sort-Object FullName -Descending | Select-Object -First 1 -ExpandProperty FullName
& $apksigner verify --verbose --print-certs .\existing-hub.apk
```

Copy the value after `Signer #1 certificate SHA-256 digest:` into `ANDROID_SIGNING_CERT_SHA256`. If the package is not installed or its APK cannot be pulled, use the exact original APK supplied by the app publisher. Do not substitute a debug APK.

### Encode the original keystore locally

In PowerShell, replace the path with the original production keystore path:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\secure\original-release.jks")) | Set-Clipboard
```

Paste the clipboard contents directly into the GitHub secret `ANDROID_KEYSTORE_BASE64`. Enter the original three passwords/alias in their matching secrets. Keep the original keystore and a secure offline backup; never upload it to the repository.

## Build and retrieve the signed APK

Once all five secrets are configured, push any new commit to `feat/full-kytyps5-native-build-20261124` (or run the workflow manually from GitHub Actions). The branch workflow builds and uploads `ps5-game-hub-production-apk-<run number>` as a private Actions artifact and does not publish a public release.

The workflow refuses to upload the APK unless:
- Gradle release signing is configured and succeeds.
- The package name is exactly `com.ps5gamehub.android`.
- `REQUEST_INSTALL_PACKAGES` is present.
- All six expected libraries are present under `lib/arm64-v8a/` and are ELF64 AArch64.
- The APK signer certificate SHA-256 exactly matches `ANDROID_SIGNING_CERT_SHA256`.

## Publish and in-app update behavior

The app checks the latest stable GitHub Release on launch and also offers a manual **Check for updates** action. Once an authorized stable release exists with exactly one APK asset and GitHub's SHA-256 asset digest, the app can download it, verify the digest, and hand it to Android's package installer. Android still asks the device owner to approve installation; Android does not permit an ordinary app to silently install its own update.

A public release must be built from `main` and published with a protected `vX.Y.Z` tag or an explicitly authorized manual publish from `main`. Do not change tag-protection rulesets to bypass this check.

## Device validation still required

A matching certificate and successful signed build are necessary, but do not prove a working emulator on every device. Install/update behavior and actual owned-game compatibility must still be verified on the target phone before describing games as playable.
