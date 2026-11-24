# Repository security hardening

This repository is intentionally public. Do not put credentials, Android signing keystores, signing passwords, access tokens, private game dumps, firmware, or console keys in Git history, issues, pull requests, or build artifacts.

## Controls maintained in the repository

- Android CI and CodeQL workflows use read-only repository access unless CodeQL needs to upload security results.
- Checkout steps do not persist the GitHub token in the local Git configuration.
- Release signing secrets are scoped to the steps that need them.
- Signed releases use the `android-production` GitHub Environment.
- Release tags must be semantic versions (for example `v1.2.3`) and the tagged commit must already be contained in `main`.
- The release workflow publishes a SHA-256 checksum alongside the APK and removes the temporary keystore at the end of the job.
- Dependabot, CodeQL, CODEOWNERS, and the security policy are maintained in the repository.

## Required GitHub owner settings

These controls cannot be enabled from the repository file tree. A repository owner must configure them in GitHub Settings:

1. **Rulesets / branch protection for `main`:** require pull requests; require at least one approval where practical; dismiss stale approvals; require approval of the most recent push; require status checks for **Android CI / Build Android debug APK** and **CodeQL / Analyze Kotlin/Java**; require conversation resolution; block force pushes and branch deletion; do not allow bypass except an intentional emergency administrator path.
2. **Tag protection:** create a tag ruleset for `v*`; restrict tag creation/update/deletion to the repository owner or trusted release maintainers.
3. **Actions:** set default workflow permissions to read-only; require approval for outside collaborators' workflows; restrict allowed actions to GitHub-owned actions and explicitly approved actions where feasible.
4. **Production environment:** create the `android-production` environment; add required reviewers and prevent self-review if available; restrict deployment branches/tags to approved release tags; store `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD` as environment secrets, not repository files.
5. **Security:** enable secret scanning and push protection if available on the account/plan; enable private vulnerability reporting; keep Dependabot alerts and security updates enabled.

## Private repository separation

A private repository is not a substitute for secret storage. This Android project also has a separate private companion repository, `PS5-Game-Hub-Android-Core`, for internal source and development. The public Android repository remains the CI/build surface. The separate `PS5-Windows-Hub-Core` repository belongs to the Windows project and is not part of this Android project. Keep signing credentials only in protected GitHub Environment secrets; never copy signing keys or tokens into either Android repository.

## Current verification limits

The connected GitHub integration can update repository files and inspect public workflow results, but it cannot administer repository rulesets, branch protection, environment reviewers, or repository creation. Those owner settings must be completed in GitHub's web interface and then independently verified.
