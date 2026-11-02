# APO RC84 | Concise habitat and species preflight

**Date:** 2 November 2026. **Branch:** `feature/apo-habitats-checklist-v144`, directly from signed RC83 code143. **Target:** versionCode **144**, `0.27.3-rc84-apo-habitats-checklist`. Original Android package, signing certificate and SQLite **28** unchanged.

## UI changes

The screen after optional Survey GPS habitat preparation is now an essentials-first layout: a short start-GPS summary, an observed-habitat list with confirmed-count label, and a single species-checklist mode button. Long coordinates and elevation context, satellite land-cover source/license, and offline import are available under disclosure controls. The full 16-habitat selector remains reachable and may be closed again. Map-suggested habitats show as **unchecked** choices; selection remains observer-confirmed only. Species summary is concise, explicitly **reference only**, with suggested-first/full mode retained. The saved checklist-mode and prepared habitat attributes are unchanged.

**Scientific rules retained:** GPS-start provenance, distinction of mapped 2020 land cover from observed habitat, site-based ranking not occurrence, true altitude source/uncertainty, full species availability, original PAIC714/WorldCover, no hidden automatic field decisions, explicit missing GPS, and all Complete/Incomplete/Not Counted outcomes.

## QA

New Python RC84 source contract; updated RC69 source safety contract; native Android14 instrumentation of Birds and Herpetofauna compact preflight, habitat confirmation and disclosure at **160%** and **200% / 320dp** with four distinct screenshots. Workflow `.github/workflows/apo-habitats-v144-emulator-qa.yml` checks original signed APK identity and immutable assets and builds Android/JVM tests.

**Status:** Source committed. Signed CI and screenshot acceptance pending, so RC83 remains latest delivered until verified. Physical handset archived-data upgrade, GNSS accuracy, taxon catalog upgrade/export and full TalkBack are not field-tested.
