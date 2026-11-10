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

## Signed CI and direct-artifact verification | 2 November 2026

- **App code revision:** `d7f151e158ef1aafd168fd5c1681055a3483e9f9`. Successful dedicated CI run **41405626317**, with Python scientific/UI regression, Java/JVM app and AndroidTest compilation, signed APK/reference checks, and Android 14 recorder and Birds/Amphibians picker instrumentation at 160% and 200%/320dp. CI generated six screenshots.
- **New directly attached APK:** `APO-v0.27.3-RC85-Recorder-Species-Picker-code145.apk` (11,757,540 bytes). Local SHA-256 **`dcaf27e8ed6c16d1bcfec0b140dbbc8f44a63e6a48d6d56b7cd0ac6f0ce4a32b`**. Extracted from Actions artifact ID **13123203193**, downloaded into the 2026-11-02 owner chat. Confirmed nonempty APK ZIP, intact manifest/dex, PAIC714 and WorldCover assets.
- **Signing:** APK Signing Block v2 signer certificate SHA-256 `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, matching original beta08 certificate. Manifest strings include app package `org.phwildlife.survey.beta08` and versionName `0.27.3-rc85-apo-recorder-species`; Gradle sets versionCode 145. CI verified package/signing as part of the passing workflow.
- **Emulator evidence:** Artifact ID **13122929336** includes `recorder_160.png`, `recorder_200.png`, `picker_birds_160.png`, `picker_birds_200.png`, `picker_amphibians_160.png`, `picker_amphibians_200.png`. Images were opened for visual inspection. At 200% font, group/view spinner names truncate; the Birds 160% snapshot shows loading before the result cards populate. The Birds 200% and Amphibians screenshots show actual species cards. Recorder retains pinned Save/Review/End and begins with observation form.
- **Still unverified:** physical-phone installation/upgrade with existing records, in-field GPS/elevation, audio/camera peripherals, migration with archived data, TalkBack and field export round trip. Draft PR remains unmerged. Direct APK links are per-chat and require fresh file access on subsequent conversations.

**Current status:** Signed RC85 development preview verified and directly delivered. RC84 is superseded as the most recent verified preview, not as a stable production release.

