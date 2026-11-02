# APO RC85 | Streamlined recorder and informative species picker

**Date:** 2 Nov 2026. **Source branch:** `feature/apo-recorder-picker-v145`, directly based on signed RC84. **Build target:** `0.27.3-rc85-apo-recorder-species`, versionCode **145**; original package `org.phwildlife.survey.beta08`, original signing certificate and SQLite schema **28** unchanged.

## User-visible changes

- Live recorder opens with **Record observation** before optional Field tools for opportunistic/non-station surveys, even at standard font sizes. Station-based units and GPS transects retain direct effort controls. Primary Species list and Photo access move closer to species/count/detection. Short labels: Field tools, ID confidence, Distance, Herpetofauna, Mammals, First detection, Environment, More details.
- Species picker removes long cautionary paragraphs from list cards and sort/site dialogs; preserves direct search by scientific, common, and synonym names and one-tap Unidentified. Information density improves: **common and scientific names, family, distribution status, IUCN and Philippine red-list fields, location evidence where recorded, site match, habitat, elevation**, with missing values omitted rather than invented. **Profile** opens existing source-rich detail; **Select** returns to recorder.
- **Habitat first** now actually sorts the complete catalog ahead of other available taxa, rather than silently excluding unmatched species. Explicit local-source-only view continues to require an area and use reviewed source records.
- Short field labels do not modify the underlying stored confidence codes: initial **Identified** still saves existing canonical `Field-identified (not expert verified)` value. No database or export schema modifications, taxon reclassification, invented occurrence, or GPS data changes.

## Safeguard copy

This update removes defensive scientific copy **on the recorder and picker interface**. Data provenance, recorded values, review status and reproducible research exports remain unchanged. Not an app-wide removal of validated GPS/effort data integrity rules.

## Acceptance and limitations

Dedicated GitHub workflow `.github/workflows/apo-recorder-picker-v145-emulator-qa.yml` runs scientific/unit tests, original signer/package/art/714-species/WorldCover hashes, and native Android14 **recorder + Birds/Amphibians picker** at 160% and 200%/320dp (six distinct screenshots). Direct verified APK only after success and actual artifact extraction. Live field GNSS, microphone/camera hardware, archived-data upgrade, full accessibility and research export roundtrip are separate unverified checks.

**Current status:** Source committed, signed code145 CI pending. Last accepted APK RC84.
