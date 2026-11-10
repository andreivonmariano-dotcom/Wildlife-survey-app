# APO RC84 | Concise habitat and species preflight

**Date:** 2 November 2026. **Branch:** `feature/apo-habitats-checklist-v144`, directly from signed RC83 code143. **Target:** versionCode **144**, `0.27.3-rc84-apo-habitats-checklist`. Original Android package, signing certificate and SQLite **28** unchanged.

## Signed RC84 acceptance

- **App/test source:** `2cfb36281bf9b89260939f14ff5c2915f75dc4f3`, GitHub Actions **41401267934 SUCCESS**. **435 Python tests**, successful Android/JVM and AndroidTest compilation, and **two Android14 instrumented habitat preflight tests at 160% plus two at 200%/320dp** for Birds and Herpetofauna.
- **Signed APK:** `APO-v0.27.3-RC84-Compact-Habitat-Checklist-code144.apk`, **11,758,144 bytes**, SHA-256 `40bfe6326d0b937337f4ce9ad3b7b0933d455d29872f6a80b5a9088c36cf00a7`, extracted from successful CI artifact **13121089719**. ZIP and embedded exact approved mountain/PAIC714/WorldCover SHA-256 hashes independently checked in local runtime. CI also verified original signer and application ID with code144 and `0.27.3-rc84-apo-habitats-checklist`.
- **Four native screenshots:** artifact **13121079780**, Birds 160% (1080×2280, SHA `2daba34694040db7a1dce420a6e6def7bb67579bb75142849fdd49558c7d93cc`), Birds 200%/320dp (960×2160, SHA `72848c3092eb475bc2c8e94e0250bec07cb67e1e5911683669e6b0037f6898e4`), Herpetofauna 160% (1080×2280, SHA `1a4a00a2e6628580892979249e47d0db8b8ede9644f75f4af2e7bbdba9347e46`) and Herpetofauna 200% (960×2160, SHA `baeb2d006c1bc9c015dea6ee3460d7bf00bfe883f24a6837e3bd45c608686e55`). All distinct. Visual inspection of 200%/320dp confirms habitat and species-checklist mode both appear before pinned Begin Survey; optional site/map details below fold and scrollable.
- **QA boundary:** RC84 is a signed **developer preview** directly delivered in the current user conversation; draft PR #86 remains unmerged. Device GNSS, actual 714-species catalog availability/ranking, off-device research export and backward upgrade with live historical surveys, TalkBack and real completion flows remain unverified.
- **Iteration history:** First emulator capture failed due duplicate image pixels caused by no visible group label and screenshot before layout settled. Both were corrected, then an outdated test button label caused one emulator failure. Final source `2cfb3628` and run **41401267934** are the acceptance record; preceding failed runs are not release evidence. Separate historical RC4 art-lineage check is incompatible with current APO source and fails independently of the dedicated signed CI.

## UI changes

The screen after optional Survey GPS habitat preparation is now an essentials-first layout: a short start-GPS summary, an observed-habitat list with confirmed-count label, and a single species-checklist mode button. Long coordinates and elevation context, satellite land-cover source/license, and offline import are available under disclosure controls. The full 16-habitat selector remains reachable and may be closed again. Map-suggested habitats show as **unchecked** choices; selection remains observer-confirmed only. Species summary is concise, explicitly **reference only**, with suggested-first/full mode retained. The saved checklist-mode and prepared habitat attributes are unchanged.

**Scientific rules retained:** GPS-start provenance, distinction of mapped 2020 land cover from observed habitat, site-based ranking not occurrence, true altitude source/uncertainty, full species availability, original PAIC714/WorldCover, no hidden automatic field decisions, explicit missing GPS, and all Complete/Incomplete/Not Counted outcomes.

## QA

New Python RC84 source contract; updated RC69 source safety contract; native Android14 instrumentation of Birds and Herpetofauna compact preflight, habitat confirmation and disclosure at **160%** and **200% / 320dp** with four distinct screenshots. Workflow `.github/workflows/apo-habitats-v144-emulator-qa.yml` checks original signed APK identity and immutable assets and builds Android/JVM tests.

**Status:** Source committed. Signed CI and screenshot acceptance pending, so RC83 remains latest delivered until verified. Physical handset archived-data upgrade, GNSS accuracy, taxon catalog upgrade/export and full TalkBack are not field-tested.
