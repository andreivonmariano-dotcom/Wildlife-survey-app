# LIKAS RC9: profile completion, comparative characters, photographic marker review

Date: 28 October 2026. Branch: feature/likas-core-fieldwork-post-rc4. Draft PR #38.

## Accepted Q1: strict 96-species completion standard

The 96 source-keyed priorities have an explicit 12-area completion register at app/src/main/assets/rc9_profile_completion_v1.json. Fields are taxonomy and source, diagnosis, differential characters, sex variation, life stage, measurements, habitat, range, behavior, dated IUCN plus separate DENR evidence, source-cleared media, and specialist review. These are evidence areas, not a binary completeness score. Fields not supported by citations remain not_assessed. The current certified-complete count is **zero**. A present source excerpt does not represent an expert-approved identification key.

Observed preliminary coverage of 96 priorities: taxonomy 96, diagnosis 49, comparisons 4 taxa, sex-linked reference 6, life stage 2, measurements 22, habitat/ecology 25, range 61, behavior/sound 3, full dual-jurisdiction conservation review 0, offline-photo representation 33, specialist signoff 0. This categorization is an audit of available evidence and may overlap within individual claims. It does not imply 96 complete profiles. Existing original species and habitat references remain separately licensed.

## Accepted Q2: controlled differential identification

The pairwise differential pack at rc9_differential_traits_v1.json initially contains eight cited characters across two pairs: Penelopides mindorensis versus Penelopides affinis, and Varanus mabitang versus Varanus olivaceus. Store anatomical region, applicable sex or life stage, values for each taxon, source URLs and limitations. The Mindoro Hornbill's male facial-skin color has differing descriptions in the Hornbill Specialist Group and IUCN 2020 assessment; preserve that disagreement. The Mindanao bill-base character has subspecies limitations. The monitor dorsal color is affected by lighting. Range is context rather than a diagnostic identification by itself.

All pairs remain published_synthesis_pending_specialist_signoff. Same-genus membership is NOT validation of species lookalike similarity. The user can open differential research from the priority library or species detail, without modifying survey identifications. Wider multi-species diagnostic key work remains pending.

## Accepted Q3: reviewable owner-photo anatomical annotations

A photo-marker editor was added for **owner-authored** iNaturalist photographs with explicit private research permission. Tap coordinates use normalized original-image geometry and preserve aspect ratio. A reviewer selects one source-backed visible-character claim, identifies themselves, taps the image and records notes. The resulting JSON record contains photo ID, SHA-256, species name, photographer, original source URL, selected trait and citation, normalized coordinates, reviewer, UTC time, and pending_expert_review status. Drafts remain in app-private files and are exported using the Share drafts control. They are NOT covered by the existing survey backup ZIP; export them before clearing app storage.

The registry rc9_approved_photo_annotations_v1.json starts **empty**. The display helper will only render approved records matching original photo ID/SHA and source/review metadata. No annotation is claimed as expert-reviewed today; no speculative anatomical boxes were drawn on original images. Third-party photographs cannot enter the owner-only editor.

## Database, rights and release

Preserve org.phwildlife.survey.beta08, SQLite schema 27, existing signing identity, all recorded fieldwork surveys and unedited original photos. Owner dreidrei photos remain all-rights-reserved with a direct copyright-holder private noncommercial research permission. Separately licensed third-party photos retain their Creative Commons rights. Photo count expected from RC8B remains 49 iNaturalist photographs, 58 bundled JPEGs total; verify actual RC9 binary before any published count claim.

Scientific Python tests, Android build, the no-data-loss code gate and package media checks must pass. Android emulator and physical-device QA of the new marker editor are pending until separately verified. Keep PR #38 draft.

Next review work: editorial signoff of species diagnoses, measurable characters, age/sex differences, geographic taxonomy conflicts, annotations on actual images, and richer habitat associations. No population, site occupancy, body traits or photo coordinates should be invented.
