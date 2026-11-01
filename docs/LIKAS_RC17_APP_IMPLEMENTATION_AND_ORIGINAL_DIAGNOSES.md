# LIKAS RC17 | Real app integration of publication notes and diagnostic comparisons

Date: 29 October 2026, Asia/Manila  
Branch: `feature/likas-core-fieldwork-post-rc4`, draft PR #38  
Targets: master requirements G-01, G-03, G-04, L and U, original SQLite 27 and app identity intact.

## Implementation

**This is Android source code, not just a research document.** `Rc11PrioritySpeciesView.java` loads `app/src/main/assets/rc17_verified_research_v1.json` and adds the source-backed facts to existing five category cards. The same class conditionally adds **Compare similar species** only for species in published comparative rows, and opens `Rc9DifferentialActivity.java` with the current scientific name. The existing general trait and licensed-photo comparisons remain.

`Rc9DifferentialActivity.java` now shows the species-specific paired characters and a single original-publication link where both species use the same source. It no longer displays the obsolete specialist-certified gate, end-of-page defensive text or redundant A/B links. The source in `rc9_differential_traits_v1.json` retains the original two hornbill/monitor pairs and adds two:

- *Platymantis pygmaeus* vs *P. naomii*. Alcala, Brown & Diesmos (1998), *Proceedings of the California Academy of Sciences* 50:381–388. Original male and female SVL sample ranges, skin ornamentation and advertisement calls. Original PDF: https://researcharchive.calacademy.org/research/herpetology/philippine_collection/phil_pdfs/casp_50_381.pdf.
- *Varanus bitatawa* vs *V. olivaceus*. Welton et al. (2010), *Biology Letters* 6:654–658, DOI 10.1098/rsbl.2010.0119. Original color-pattern combination, geographic study region, and genetic diagnostic evidence.

## Published biological details integrated

**16 source-scoped facts** across five priority taxa (two frogs, one monitor lizard, one mammal, one bird):

- *Platymantis pygmaeus* (1998 original description): adult male 14.1–15.7 mm SVL, one adult female 15.0 mm, smooth dorsal skin, brief 'tet-tet' call.
- *Platymantis naomii* (1998 original description): adult male 20.3–23.9 mm SVL, female 21.6–28.0 mm; tubercles/ridges, 'psik' call, original Mt. Banahao montane forest sample.
- *Varanus bitatawa* (Welton et al. 2010): contrasting black and gold, dark juvenile throat, central/northern Sierra Madre. Original DOI 10.1098/rsbl.2010.0119.
- *Carlito syrichta* (Bejar et al. 2026), *Folia Primatologica*, DOI 10.1163/14219980-bja10081: 44 sleeping sites of six tracked tarsiers, measured Siargao mangrove roost substrates, three tagged adult males with site-specific 3.16 ha average MCP range.
- *Penelopides panini* (Mynott et al. 2021), *Avian Research*, DOI 10.1186/s40657-021-00303-3: primary vs secondary forest density estimates and 2,673-individual estimate for **Northwest Panay Peninsula**, with the published sample bounds.

No wildlife record or source-specific measurement is promoted to all-Philippines abundance, universal body-length limits or survey-site occurrence. Existing source citations and original observations are preserved.

## Data, code and QA

- Source bibliography `rc14_96_species_literature_index_v1.json` covers the unchanged **96 priority species** and has **250 distinct species–source links**, up from 245, with **13** species having separately verified 2025–2026 papers. Remaining **83** individual latest-publication searches are open.
- Four comparison pairs, 14 comparative traits in `rc9_differential_traits_v1.json`. Pair validity is based on published source links, not a specialist approval process.
- `scripts/test_rc17_original_comparisons.py` checks all 96 source keys, 16 facts, exact 4 pairs and app button integration.
- `Rc11SpeciesAccountInstrumentedTest.java` tests real *Platymantis pygmaeus* source card and direct comparison activity, supplementing the prior ten Android tests. `scripts/run_emulator_rc7_qa.sh` expects **11** tests.
- New version target **0.27.3-rc17-original-comparisons-preview**, versionCode **69**. Keep package `org.phwildlife.survey.beta08`, pinned certificate fingerprint `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`, SQLite 27 and bundled photo rights intact.

## Release conditions

Do not say the entire 96-species research program is complete, that original comparisons exist for every species or that the physical phone passed. GitHub Android build, source tests, emulator upgrade/data preservation, archive inspection and same-certificate APK verification are distinct gates. Direct APK link only for an exact currently verified file.
