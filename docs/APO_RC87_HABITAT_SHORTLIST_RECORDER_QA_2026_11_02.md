# APO RC87 | Site habitat shortlist and faster field identifications

## Signed CI and direct artifact acceptance | 2 November 2026

App/test code `e3d60847ac25be7239a7556473d6b604e388d6ed` passed GitHub Actions `41419356860`. Python discovered **448** passing source/scientific tests, JVM/App/AndroidTest compilation and original APK signer/package and PAIC714/WorldCover/locked-art checks. Android14 (API34) instrumented 160%- and 200%-font recorder/Birds/Amphibians species picker tests succeeded, including `montaneContextHidesCoastalTernButAllSpeciesCanFindIt`. Six screen PNGs were uploaded and visually inspected. At 200%, `PAIC · 714`, `ZIP`, Recent and Unidentified are readable; the field recorder's Save/Review/End actions remain visible. Screen captures of the selected montane state were not generated separately: its behavior is supported by the passed instrumentation, not by those six default-view screenshots.

Signed binary from artifact `13128823267`: `APO-v0.27.3-RC87-Habitat-Shortlist-Interactive-code147.apk`, 11,763,844 bytes, SHA-256 `6eb23398eb18565f3e1694842881c509292ed93efeb599698d6c80c5f4067a6f`. User-visible binary was extracted, integrity checked, and attached directly in the originating chat. All three embedded immutable reference/art asset digests matched; emulator screenshots artifact `13128683666`. This is a **developer preview, not physical-device field certification**. The source-only status from the initial RC87 drafting below is superseded by this signed acceptance entry.


**2 November 2026, Asia/Manila.** Draft source branch `feature/apo-rc87-habitat-shortlist-recorder-v147` derived from verified signed RC86, code146/SQLite29. Target is `0.27.3-rc87-apo-habitat-shortlist`, versionCode **147**; APK remains **unshipped** until signed CI, emulator screenshots and direct delivery are checked.

## Observed defect and source evidence

The owner selected **Montane forest** in Species Picker > Site context, but **Aleutian Tern** remained listed. RC86 `Habitat first` only sorted by **survey-preparation** habitats, not the newly selected site habitat, and no habitat filter was applied. The unchanged owner-authorized 714-taxon CSV describes `Onychoprion aleuticus` as **Coastal and intertidal areas, marine waters** with display elevation **0–30 m**, not montane forest. These are coarse guide attributes, not independent absence or habitat-selection evidence.

## RC87 change

- In the active PAIC catalog, an observed site habitat now enables **Habitat shortlist** by default. The shortlist actually restricts results to species whose source guide has a named compatible habitat. Montane forest guide matches prioritize `Montane habitats`; generic `Forest` is accepted only as a **broad category**, not montane confirmation. Coastal/marine-only Aleutian Tern is excluded from the default montane shortlist.
- **Show all species** instantly restores the complete offline catalog without changing the recorded site habitat. Omitted species are *not* labeled absent, and missing or broad guide data cannot prove presence. The archived Philippine catalog remains fully searchable and is not silently assigned PAIC guide categories.
- Shortlist matching is separate from source-reported geographic occurrence, observed detections and evidence-based analysis. No observations or checklists are modified.
- Two readable full-width picker controls preserve the site editor and explicitly expose shortlist/all toggling. The 200% font-size catalog spinner shows compact `PAIC · 714` or `Philippines` with full accessible labels.
- A **Recent** action in both recorder and picker lists up to six saved raw identification names for the selected wildlife group. Choosing one fills only the species name/search query; never copies count, confidence, GPS, photos or effort. Recorder defaults confidence to Not assessed for a reused name.
- In enlarged text the recorder export control uses `ZIP` with a full accessible description, avoiding truncated `All data`. `Complete`, `Incomplete`, `Not Counted`, persistent End Survey, original artwork, package ID, SQLite29 and 714/WorldCover reference bytes remain unchanged.

## Verification plan and gates

- New source assertions `scripts/test_apo_rc87_habitat_shortlist.py` check the Aleutian Tern CSV source, the actual filtering branch, observed habitat-to-guide mapping, and recent-name nonmutation.
- Updated `PaicHabitatPolicyTest` covers montane match, broad forest match, coastal tern exclusion, wetland match, and missing evidence.
- New GitHub Actions `.github/workflows/apo-rc87-habitat-shortlist-qa.yml` runs source/Python/JUnit/Android compile and original signing/asset checks, followed by existing actual Android 14 emulator recorder/picker tests at 160% and 200%/320dp.
- Pending: confirm workflow success, inspect all screenshots, test opening site context and actually switching filters on Android emulator, archived catalog behavior, in-place SQLite28→29 data-preserving upgrade, real GNSS/photo capture, export round-trip, TalkBack and field acceptance.
- Do not merge into historical default `main` or claim a signed release based on source commits.
