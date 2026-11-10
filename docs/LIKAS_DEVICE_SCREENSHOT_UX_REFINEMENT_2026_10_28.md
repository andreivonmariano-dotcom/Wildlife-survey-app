# LIKAS | Device Screenshot UX Review and Field Guide Refinement

**Review basis:** Actual Android screenshots supplied by the owner on 28 October 2026. These showed Home, bird survey setup, Species Library hub, species lookup and dropdown, Philippine Eagle account, Habitat and Ecology full scrolling guide, Projects and Sites, Saved Data, Survey History and Fieldwork Dashboard.

**Scope:** All three accepted owner questions: continue curated Philippine endemic/threatened wildlife content, make habitat guides more visual and usable, and smooth the minimalist startup-to-Home experience. No new survey-database schema.

## Problems observed on physical-device screenshots

1. **Species search:** Empty-query dropdown showed arbitrary alphabetical taxa in an oversized 300dp popup with very tall rows; common/scientific names were truncated. Search-only page looked blank until a name was known.
2. **Species account:** Eagle photo cropped aggressively and large provenance/URL paragraphs competed with identification context. Many source paragraphs needed larger type and progressive disclosure.
3. **Habitat and Ecology:** Reference descriptions, measurements and personal station entries shared one long scroll. The nine guide cards lacked filtering, and full text grew unwieldy. Media/attribution made pages much taller.
4. **Home:** Functional grouping was good, but the jump from dark minimal startup into Home did not visually connect the LIKAS wordmark.
5. **Cross-screen inconsistencies:** Survey setup remains an extensive form. Dashboard and History Back buttons use inconsistent placement. These require a separate, measured interaction pass rather than opportunistic changes to scientific workflows.

## Owner-accepted implementation on current core branch

- **Species lookup:** `SpeciesReferenceSuggestionAdapter` now requires two typed characters, displays at most eight results, uses compact 48dp rows, and does not open an arbitrary alphabetical list for blank input. The library screen has a wildlife-group selector with an explicit filter label. The dropdown button gives guidance for an empty query. The read-only library exposes a bundled **Browse detailed field guides** directory, with four group-leading shortcuts and a disclosure for the rest.
- **Species content:** Added `Crocodylus mindorensis` and `Cacatua haematuropygia` to the eight earlier introductory accounts, for **ten** source-cited, offline, detailed species accounts. Sources include DENR Philippine Crocodile recovery plan (2005), University of Michigan Animal Diversity Web, DENR WildALERT cockatoo description and Philippine Cockatoo Conservation Programme report (2017). Their older research dates remain visible; not current population updates, validated keys or local detections. Distinct national taxonomic checklist data remains intact.
- **Species reading:** Ecology and overview remain near the top; behavior, field documentation, cautions, and source metadata are behind **Field notes & source references** disclosure. Body text on library content uses 15sp with improved line spacing. Media are displayed using FIT_CENTER rather than cropping, and photo source/license URLs reside in an accessible expandable credit view.
- **Habitat reading:** `HabitatRecordsActivity` now separates **Habitat guide** and **My site records**. The guide filters Terrestrial, Edaphic, Coastal, Aquatic and Human-modified groups, shows short two-line previews, and supports a single expanded card at a time. Full article text and JPEG loading occur on demand. Each record includes scientific caution and recommended field measurements. Saved station/site records use the separate tab and remain unaffected.
- **Additional real photo:** The limestone-karst habitat guide references an attributed CC BY 4.0 photograph from Bacuit Bay, Palawan, by Vyacheslav Argenberg. Original Commons file title, license, source URL and photographer remain in `offline_photo_manifest_v1.json`. Whether its JPEG thumbnail actually reaches the APK depends on the CI media retrieval result; failure is reflected by an unavailable-image message. The original six Commons candidates remain licensed and attributed.
- **Brand transition:** `LikasLaunchActivity` remains minimalist with accessibility-aware fade. The first Home header softly appears over 230ms only on startup entry, respecting system animator settings. No forest animation or GPS is involved.

## Verification and limits

- New Android source, cited species JSON and habitat photo manifest are committed on `feature/likas-core-fieldwork-post-rc4` (draft PR #38).
- Python source contracts, Android/JVM compile, media retrieval, CI artifact, signer continuity, application identity and actual bundled-photo count **must be checked** before advertising a newly built APK.
- The trusted RC4 PR lineage gate still contains older forest-startup requirements and fails independently of the scientific-core build. Do not claim merge acceptance from a source build alone.
- Scientific profiles are introductory source-checked text, not full taxonomy-specialist-reviewed keys. Existing checklists comprise many more taxa than these ten accounts.
- Screenshots provided are **before** this specific refinement. A new installed-phone screenshot is required to judge actual final spacing, popup size, keyboard interactions and device performance.
- Current-version complete backup/restore and real-device destructive-data QA remain prerequisites for field release.
