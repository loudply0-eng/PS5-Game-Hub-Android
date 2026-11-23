# Security Policy

## Reporting vulnerabilities
Do not publish exploit details, credentials, signing keys, private game dumps, firmware, or personal data in public issues.
Use GitHub's private vulnerability reporting feature if enabled. Otherwise contact the repository owner privately through GitHub.

## Secrets
Never commit keystores, passwords, access tokens, API keys, private keys, game dumps, firmware, or console keys. Keep release-signing material outside the repository and use protected GitHub Actions secrets only when a release workflow is intentionally configured.

## Releases and updates
The Android app checks the official GitHub Releases API. Users must review and approve installation through Android. Publish only project-built, signed APKs with release notes and SHA-256 checksums.
