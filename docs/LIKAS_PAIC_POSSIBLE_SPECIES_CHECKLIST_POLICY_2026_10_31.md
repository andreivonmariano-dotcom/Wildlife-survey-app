# LIKAS | PAIC possible-species checklist product standard
**Owner-approved 31 October 2026. Scope: all future PAIC or explicitly defined biogeographic island-group species checklists.**

## Purpose and accepted inclusion threshold
The Species Library's PAIC checklists are **possible species for an entire selected biogeographic region**, not verified provincial inventories, current occupancy maps, native biodiversity richness estimates, or a real-time prediction of sightings at the device's GPS fix. The owner does **not** require an independent primary-locality verification for each taxon before displaying it as a *possible species*. A documented regional claim in a named reference checklist or a published locality source is sufficient for a **source-based candidate**, assuming it is neither solely negative nor an unsupported ambiguous assignment.

Original national Philippine checklist membership alone does not justify Greater Mindanao PAIC membership: a source must actually supply some plausible PAIC or member-island link. Preserve original taxon key, source identity/version, original scientific name, and URL. Never invent island localities, sources, taxonomic identity, or biodiversity status to fill a gap.

## Minimum data controls
- Group by stable *candidate display ID* (not necessarily a taxon concept) and preserve all original taxon keys and names. Collapse repeated source trinomial labels under the matching binomial **only for display**. Keep the original subspecies labels accessible, never delete or rewrite their scientific source.
- A genus-only `Ophiophagus` or an unresolved `sp.` is **not** a binomial species. Keep it in a separate unresolved taxa register for identification help, without inflating species-level candidate counts.
- Explicit negative/excluded and questioned region statements are **not** affirmative PAIC candidates on their own. Retain them in a visible deferred register with rationale and source. Do not turn an exclusion into absence evidence.
- Vagrants, migrants, introduced/non-native, offshore/coastal, historical and possibly extirpated taxa may be browseable as *source-based possible or recorded historically* **only with prominent separate qualifiers**. Do not call them native, currently extant or resident on that evidence alone.
- Display source geography as reported. A cited study from an island or precise locality in Greater Mindanao supports possible presence **somewhere** in the working PAIC; it says nothing about the other member islands, other habitats or the field observer's station.
- Keep cited DOI/source links and distinguish references to source-based candidates, historical links, negative qualifiers and literature metadata. A citation link is not automatically positive proof of current presence.
- Missing species do not imply absence. Candidate totals describe *coverage of the installed reference collection*, not actual PAIC richness or sampling completeness.

## Regional scope and privacy
Regional navigation checklists are PAIC/island-group based only. **Provincial checklists and provincial species richness products are cancelled.** Precise site and province names can remain as original source provenance and do not create a provincial checklist.

WBCP and Emerson Sy checklist material came from the owner's research files. Keep source-derived private row-level compilation and citations in directly delivered private artifacts; do not commit this derived checklist into the public GitHub repository without resolving license and redistribution terms. A future public Android APK that bundles this private possible-species dataset requires a separate rights decision. These requirements do not restrict sharing the user's current private research workbook with the user.

## Greater Mindanao accepted v1.0 snapshot (31 October 2026)
The owner accepted **possible**, rather than independently verified occurrence, as the completion criterion. The pinned v0.3 research register held **410 source taxon keys**, and conservative source-semantic rules yield **400 possible binomial display names** (**186 birds**, **50 amphibians**, **153 reptiles**, **11 mammals**) from **402 included source keys**. **Seven explicitly negative/partially excluded/questioned keys** and **one genus-only `Ophiophagus`** key remain deferred. Two pairs of trinomial source names produce two display-name deduplications: `Cyclocorus nuchalis` and `Rhabdophis auriculatus`. The entire 410-key original register, 111 evidence/qualifier links, and 55 bibliography rows are retained privately, without masking their varying research quality.

The finalized private deliverables are:
- `LIKAS_Greater_Mindanao_PAIC_Possible_Species_Checklist_v1.0_2026-10-31.xlsx` (7 sheets)
- `LIKAS_Greater_Mindanao_PAIC_Possible_Species_Checklist_v1.0_2026-10-31_Offline_Data.json` (ready for private/offline candidate-list loading)
- matching Candidates and Withheld CSVs, a README and a ZIP research/import package

The final XLSX SHA256 is `b0d1829a365e4f3dcef914108e50b1371750496fa08f87cf15ea7b8a6e9fdb66`, JSON SHA256 `bc033f6adab4a7789ed686adfb37e8818d3d91993693e29cb1d19a97959499e6`, ZIP SHA256 `7763ea6b32c4ca36bc7b3787b73a4bcda7a29d52429013559b45633bc0e9db3c`. Original v0.3 private input register, evidence and bibliography remain unchanged. No SQLite, app code, Android asset or APK modified in this approval step.

**Completion status:** Greater Mindanao *possible-species reference edition* v1.0 **finalized for the owner's candidate-list purpose**. It is not a completed regional faunal census, positive occurrence database or unrestricted public-release dataset.


## Greater Mindanao frozen edition v2.0 | 31 October 2026
**New latest accepted edition:** `LIKAS-GMND-PAIC-POSSIBLE-LIKELY-2026-10-31-LOCK-2.0` supersedes v1.0 for **private research/reference candidate browsing**. Exactly **638** distinct named binomial species: Birds 349, Mammals 83, Amphibians 51, Reptiles 155. **Likely (461)** uses explicit PAIC/member-island literature scope, **Possible (177)** uses WBCP published widespread Philippine range with no geographic contradiction or a questioned PAIC statement. These are descriptive evidence categories, not detection probabilities, presence at a GPS fix, current native/resident fauna or exhaustive biogeographic richness. **Source-based additions:** 162 WBCP widespread birds, one questioned named bird, 72 Field Museum Mindanao mammal guide species and three Samar 2026 herps. No genus-only or sp. records included. Original names/taxon source keys and introduced, migratory, marine and historical qualifiers retained; no per-province list revived. Immutable private XLSX/CSV/JSON/reproducibility ZIP validated and hashes are in `docs/LIKAS_GMND_PAIC_FINAL_LOCK_V2_2026_10_31.md`. Dataset is **locked**, but not added to installed app or rights-cleared for public distribution. Research-only metadata commit.
