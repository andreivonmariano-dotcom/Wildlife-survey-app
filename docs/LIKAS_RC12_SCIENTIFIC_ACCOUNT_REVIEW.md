# LIKAS RC12 | Scientific account review standard and 96-taxon register

**Date:** 29 October 2026 (Asia/Manila)  
**Scope:** Species evidence and scientific review only  
**Previous verified preview:** RC11C versionCode 62  
**New intended source preview:** RC12 versionCode 63  
**Stable package:** `org.phwildlife.survey.beta08`  
**SQLite version:** 27, untouched

## Scope and exact count

The repository contains **96** priority species, 24 each across birds, amphibians, reptiles and mammals. The owner previously referred to 92, but no source-defined four-species exclusion is documented. All 96 existing source identities remain intact.

The scientific review register at `app/src/main/assets/rc12_species_scientific_review_register_v1.json` covers **every one of the 96 names** and preserves the original `sourceTaxonKey`, group, family and 12 RC9 source-readiness fields. This is an evidence inventory, **not a declaration of 96 scientifically reviewed species**.

The RC8 evidence matrix supplies 467 source statements, including many taxonomy identities and evidence-limit notes. The three RC10 research packs contribute 64 claims across 12 species. The RC12 original-publication pilot contributes 16 additional cited claims for **three** of those existing priority species. None of the new claims is expert-certified. Exactly 0 of 96 species currently have named independent expert sign-off.

## Standard account sections

The compact, icon-led account template already displays *only cited and relevant* material: Identification, Habitat, Range, Natural history, and Conservation. The original citation and review checklist are visible by opening **Sources & details**, then **Scientific checklist**. No unsupported placeholder statement should be inserted into a visible card just to fill an empty section.

For each of 96 species, require a full scientific review against these independent gates:

| Gate | Required evidence | Completion condition |
|---|---|---|
| Taxonomy | Source edition, accepted scientific name, authority, family and synonym resolution | Checklist identity checked against source and named reviewer |
| Diagnosis | Original or appropriate revisional diagnosis and combined differentiating characters | Expert checks description against species complex |
| Differential | Confusable Philippine taxa, comparative morphology, sex and age | Negative and positive character basis, with original citations |
| Sex | Diagnostic sex variation and limit of sex inference | Published sample or justified not applicable, signed |
| Life stage | Juvenile, immature or larval appearance where relevant | Published ontogeny, or verified not applicable |
| Measurements | Measurements, sample size, units and sex/stage | Ranges distinguished from holotype measurements |
| Habitat | Habitat type, vegetation and microhabitat with methods | Distinguish sampled locality from taxon-wide habitat |
| Range | Published islands/regions, source and year | Historic and uncertain records marked as such |
| Behavior | Feeding, reproduction, vocalization or activity with source | No unsupported activity or occupancy inference |
| Conservation | Dated IUCN assessment and separate Philippine DENR legal status | Both authorities and dates independently confirmed |
| Media | Rights-cleared photo and taxon ID review | Attribution, locality safety and suitability approved |
| Final review | Named taxon specialist, reviewer date, conflict log and citations | Signed approval only after previous gates pass |

The register deliberately carries forward original `sourceState`, `existingEvidenceCount` and `expertApproved:false` per gate. It never upgrades an item merely because the app gained a note. The source reader shows existing evidence in an expandable checklist only.

## First original-publication pilot

`rc12_primary_diagnoses_v1.json` stores **16 short, paraphrased statements**, source locations, URL, source type and review state:

- **Baletemys kampalili**: Rowsey et al. (2022), *Journal of Mammalogy* 103:1259–1277, DOI `10.1093/jmammal/gyac057`. Six notes on snout, coat, tail proportions, measurements, comparative craniodental evidence, the *two sampled collection localities*, and highland forest. One-site observations are not converted to a range map or universal elevational bounds.
- **Limnonectes beloncioi**: Herr et al. (2021), original taxonomic treatment of the new Mindoro fanged frog in *Ichthyology & Herpetology* 109, publicly indexed at `https://zenodo.org/records/7850057`. Six notes include single-pulse advertisement notes versus two-pulse *L. acanthi*, finger and snout comparisons, interrupted dorsolateral ridges, holotype SVL **74.9 mm**, and the Mindoro/Semirara range. The holotype measurement is NOT presented as a species-wide adult interval.
- **Varanus mabitang**: Gaulke & Curio (2001), *Spixiana* 24:275–286, BHL `https://www.biodiversitylibrary.org/part/66081`. Four claims paraphrased from the comparative diagnosis as transcribed by The Reptile Database. The source type is explicitly **published_diagnosis_secondary_transcription**, not directly verified from the original original-language plate. Strongly keeled ventral scales versus smoother *V. olivaceus* scales are an evidence note pending specialist check.

This pilot is a scientific method demonstration, not a completed morphology review of all 96.

## Data standards and editorial rules

Each biological claim must carry scientific name/source key, source author, publication year if known, direct URL/DOI, source location (figure, page, table or section), source type, concise paraphrase, field limitation, and independent review state. Never invent a source, body range, GPS locality, taxon presence, legal category or identifying photo. Preserve published conflicts and previous identifications rather than overwriting historic observations.

Avoid repeated caution text in front-page cards. Place material exceptions near the affected trait, and all technical review metadata in the source drawer. Separate **checklist identity**, **evidence cited**, **specialist approved**, and **field-tested**. An unreadable or missing evidence file cannot be treated as approval.

## Verification

- `scripts/test_rc12_species_scientific_review.py` checks all 96 source keys, twelve fields per species, zero expert certifications, correct original claim links, 16 pilot statements and URL/source provenance.
- New Android source viewer loads original-treatment pilot before the existing 64 RC10 notes and RC8 evidence. It does not change old evidence packs.
- A collapsed checklist shows present versus unassessed evidence without promoting scientific certification.
- Android APK compilation, offline media checks, certificate continuity and signed in-place SQLite v27 upgrade still gate delivery. Review of source data is not a replacement for a taxonomist's documented approval.

## Next editorial batches

Work taxon-by-taxon from original descriptions and modern revisions, with priority on the 47/96 existing profiles without diagnostic source coverage, then differential, sex, ontogeny, habitat, range and separately dated global and national conservation assessments. For conflicts, add a reviewer note and keep the earlier source intact. Maintain both machine-readable review register and human-review notes.
