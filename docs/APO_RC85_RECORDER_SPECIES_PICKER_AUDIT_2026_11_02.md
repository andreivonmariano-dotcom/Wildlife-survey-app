# APO / LIKAS RC85 | Live Recorder and Species Picker Audit
Date: 2 November 2026 (Asia/Manila)
Baseline: draft PR #87, branch `feature/apo-recorder-picker-v145`, versionCode 145, SQLite 28.
Scope: source-level review of `MainActivity`, `SpeciesPickerActivity`, `MindanaoMasterImporter`, `BundledPrivateReferences`, `BundledCatalog`, `FieldQuickActions`, `Taxa`, related source policy, six Android 14 CI screenshots, delivered RC85 APK and owner's 714-species workbook. This is an **audit, not a source-code fix or a new release**.

## Checklist verification
- Extracted `assets/paic714_master.csv` from the verified RC85 APK. Compared scientific names and all nine display fields to the uploaded `LIKAS_Mindanao_PAIC_Final_Checklist_714_Species_2026-11-01.xlsx`: 714/714 distinct names; 0 field differences (allowing equivalence of blank and em dash when evaluating missing content).
- Group counts match: Birds 408, Reptiles 154, Mammals 99, Amphibians 53. Source rows have 432 DENR fields and 24 IUCN fields represented by the em dash.
- These checks establish **packaged file consistency**, not the current active SQLite catalog on a real phone.
- The CI picker test uses `ActivityScenario.launch(SpeciesPickerActivity)` directly. It bypasses the normal `LikasLaunchActivity` reference bootstrap. Its screenshots consequently show the broad Philippine reference catalog (765 birds, 121 amphibians), including a West Visayan frog with the region label Mindanao. This is a **test setup limitation**, not proof that a normal launch fails.
- Normal launch invokes `BundledPrivateReferences.ensureInstalled` before Home, and `MindanaoMasterImporter.install` replaces the active SQLite taxon catalog with the 714 PAIC source. An end-to-end launch -> survey -> species picker -> active 714 taxonomy test remains missing.

## Priority findings

### P0 | Prove that field-mode catalog uses the installed PAIC reference
The 160/200% emulator picker acceptance shows the broad pre-bootstrap catalog and therefore did not test the actual intended 714-species workflow. Create a fresh-install and upgrade end-to-end acceptance that enters via the real startup, verifies 714 active records and exact group counts, then opens the picker from a survey. Cover bootstrap failure/retry and persistent app data.

### P1 | The PAIC installation replaces the broader search library
`MindanaoMasterImporter.install` archives then clears `taxon_catalog`, synonyms, occurrence evidence and habitat profiles before installing the 714. Its picker then provides "All species" and "Habitat first" within this active catalog, but no direct national-library fallback for legitimate observations beyond the PAIC roster. Recommend two non-destructive catalogue scopes: "Mindanao PAIC" (default for Mindanao survey) and "Philippine species" (optional). Keep raw user-entered identifications and unresolved taxa available in either mode. Requires migration/source policy review, not a cosmetic label change.

### P1 | Species selection does not return an authoritative taxon identifier
`SpeciesPickerActivity.select` returns a formatted `species_label` and `species_group`, but not `taxon_key`. `MainActivity.save` parses the text using `Taxa.identify` and re-infers `name_source` from a scientific-name SQL lookup; `field_records` does not save the source taxon key. This can lose exact checklist identity, especially when versions/synonyms differ. Pass and persist the selected key with its source version, while preserving the original raw text. Do not backfill unknown historic IDs without an explicit reconciliation.

### P1 | Direct common-name entry can bypass the catalog identity
The recorder species entry has autocomplete threshold 100 and permits free text. `Taxa.identify` resolves exact common names only from the small hard-coded starter list; a valid PAIC common name typed manually without selecting a result can be saved with an empty scientific name and `user_entered` provenance. Add actual bounded inline catalog suggestions, while retaining unrestricted raw text for unresolved observations.

### P1 | Implicit confidence may exceed the field action
"ID confidence" is behind More details and defaults to "Identified", which saves `Field-identified (not expert verified)` even when a user has simply typed text. Keep confidence available in compact form or choose an explicitly neutral default; never silently change historical confidence codes.

### P1 | Species-specific form values can leak across consecutive observations
After save, `MainActivity` resets species, count, distance, notes, habitat, confidence, life stage and some distance fields but **not** `microhabitatEntry`, the amphibian call index, herpetofauna height, or mammal perch height. Those values may appear again on the next observation. `hasUnsavedObservation` omits these observation-specific inputs as well. Define which values are site/visit defaults and which are detection-specific, reset the latter or make intentional reuse visible, and include them in dirty-draft detection.

### P1 | "ZIP" header action has a misleading scope
The recorder header shows "ZIP" at large fonts, but `writeResearchArchive` exports all projects, surveys and observations without filtering by active `eventId`. Replace the abbreviated header with an explicit "Export all data" action in Analysis/Exports, or add a clearly labeled current-survey export with actual filtering. Preserve existing research ZIP behavior and data completeness.

### P2 | Missing checklist statuses appear as literal dashes
The PAIC CSV represents unknown IUCN/DENR values with `—` for 24/432 taxa respectively. The importer retains that text and `addSpecies` checks only for empty strings, so cards will display "IUCN: —" and/or "PH: —". Normalize display-only null/dash handling, while preserving source values. Prefer explicit "DENR" over ambiguous "PH"; expand codes in the profile.

### P2 | "Habitat first", Sort, and "Site match" do not form a clear model
The `views` spinner controls habitat-based ranking; a separate Sort dialog controls name/family/genus ordering. Both change the effective ordering. "Site match" appears for any positive `PaicHabitatPolicy.ranking`, including elevation-only overlap, and does not require locality evidence. Combine controls under a compact "Order: Name / Habitat first" choice. Label ecological overlaps "Habitat/elevation overlap" rather than implying verified species presence.

### P2 | The picker allows editing visit habitat/elevation within species selection
The Site dialog changes habitat and manual elevation and writes to `StationEnvironmentStore`. This overlaps the preceding Habitat & Species preflight and may unexpectedly modify visit context during a lookup. Display current site context read-only by default; put intentional edits behind an explicit "Edit site conditions" action and confirm changes.

### P2 | Duplicate photo actions and unclear capture pathway
The recorder always displays "Add photo" (`ACTION_OPEN_DOCUMENT`). In Herpetofauna and some Mammal methods, the configurable method quick action is also PHOTO, presenting the same file-attachment pathway twice. Keep one "Attach photo" action visible, remove the duplicate quick option when it offers no different capability, and add actual in-app camera capture only as a distinct supported action. Pending image label should say "Photo ready to attach" until the observation is saved.

### P2 | Layout and automation checks at large fonts
At 200% font, native group/view spinner labels truncate ("Amphi...", "All sp..."). A Birds 160% screenshot was saved before results finished loading. Use full-width group tabs for two herpetofauna groups and an explicit readable ordering control. Android acceptance should await loaded, actionable species cards and verify selection and return to recorder, not just a non-empty status label.

### P2 | Observation timing and pause control need method-aware access
For opportunistic methods, "Field tools" can hide Mark time/Pause effort. If a detection is not marked, saving uses the current save time; this can differ from the first detection event. Expose Mark detection near the observation form, retain accessible Pause effort, and keep the more specialized bearing/tools in the disclosure. Do not invent detection timing.

## Keep
- Pinned Save Observation / Review / End Survey dock and accessible Complete / Incomplete / Not Counted outcomes.
- Offline species browsing, common/scientific/synonym lookup, explicit unresolved IDs, and unrestricted raw entries.
- Full species detail/Profile as a secondary path; actual checklist fields and sources remain in the underlying data.
- Optional method-specific forms, station/effort/GNSS provenance, native field controls and archived data compatibility.
- Separate immediate "Unidentified" paths on recorder and picker are contextually useful; do not remove either solely because it is accessible on two screens.

## Proposed screen structure
Recorder: compact status and pause -> **Observation** (species search/list, count, detection type, Mark detection, attach photo) -> expandable method-specific measurements and evidence -> pinned Save / Review / End. Put full research export in Analysis & Exports.
Picker: header/search -> clear taxon group -> scope ("PAIC" / "Philippine species", after source-policy change) -> order ("Name" / "Habitat first") -> clean cards (name, scientific name, family, concise distribution and available conservation/habitat/elevation fields) -> Profile / Select. Site conditions are contextual, not an unexplained secondary survey editor.

## Verification and release status
Verified: embedded APK CSV versus uploaded 714 XLSX, source inspection of involved classes, six existing screenshot files, official RC85 APK SHA-256 `dcaf27e8ed6c16d1bcfec0b140dbbc8f44a63e6a48d6d56b7cd0ac6f0ce4a32b`, previously passed CI run 41405626317.
Not verified: startup-to-picker 714 activation and accurate results on a normal installed phone, in-field GPS/timing, camera hardware, archived-data migration, research export roundtrip, TalkBack, or fixes to these audit findings.
**No app code, SQLite schema, APK version, signing, bundled species file or artwork changed by this audit.**
