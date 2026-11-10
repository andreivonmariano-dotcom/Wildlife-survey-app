# Archived CI definitions

These six historical Android CI workflow definitions are retained as immutable review evidence because their RC85–RC86 and older regression tests inspect the original jobs and assertions. They no longer run as GitHub Actions workflows. The current Android QA and signed APK build are maintained in `.github/workflows/apo-quality-gate.yml` and `.github/workflows/apo-signed-apk.yml`. Preserve the archived YAML contents as source provenance; change a historical test only when its scientific or data-safety meaning is reviewed.

The pre-integration default branch's historical `build-apk.yml` and `likas-pr-lineage.yml` were preserved as `main-legacy-v015-build.yml` and `main-legacy-rc4-lineage.yml`. They are no longer active GitHub Actions workflows. The current two workflows are the only live workflow files.
