# LIKAS RC16 | Species diagnoses and filtered research bibliography

Date: 29 October 2026, Asia/Manila. Branch: `feature/likas-core-fieldwork-post-rc4`, PR #38, draft.

**Owner-selected scope:** Continue original publication diagnoses for amphibians/reptiles, published mammal and bird habitats, ranges and population studies, and compact publication-year/research-topic controls inside the existing Sources & details drawer. No specialist approval. Avoid defensive and padded app prose.

## Publication claims added

`app/src/main/assets/rc16_publication_batch_v1.json` records 14 paraphrased, source-scoped claims for nine of the original 96 species:

- *Platymantis insulatus*, original type and South Gigante karst forests cited via the AMNH curated taxon account, which identifies Brown & Alcala's 1970 original description.
- *Platymantis naomii*, Mt. Banahao montane type locality and Mt. San Cristobal distribution, cited from AMNH's account of Alcala, Brown & Diesmos 1998.
- *Platymantis spelaeus*, limestone cave type locality in southern Negros, cited from AMNH's account of Brown & Alcala 1982.
- *Varanus palawanensis*, description in Koch, Gaulke & Böhme 2010, Zootaxa 2446:1–54, DOI 10.11646/zootaxa.2446.1.1, distinguishes Palawan populations formerly placed under *V. marmoratus*.
- *Brachymeles hilong*, pentadactyl limbs, scale rows and 59.9–81.5 mm SVL from a published revision **as transcribed by The Reptile Database**. The database transcription is labeled curated, not a direct peer-reviewed primary paper.
- *Acerodon jubatus*, Ramos & Nemenzo-Calica 2025, Davao Research Journal 16:117–129 DOI 10.59120/drj.v16i2.389. The Luban Island roost once identified as *A. jubatus* is now considered possibly *Pteropus* species or a mixed *Pteropus* colony. No new confirmed *A. jubatus* population is claimed.
- *Rusa alfredi*, Javier et al. 2025 GigaByte DOI 10.46471/gigabyte.150 describes present wild populations on Panay and Negros based on earlier published reports.
- *Pithecophaga jefferyi*, Pascual et al. 17 Oct 2026 Scientific Reports DOI 10.1038/s41598-026-76205-5. The study compares genomic variation of 20 wild-captive and 15 captive-bred eagles, not a wild population census.
- *Cacatua haematuropygia*, Widdman et al. Silliman Journal 42(2), **2001 issue uploaded 2022**, documenting Rasa Island mangroves/coastal forest and a **2000** breeding-pair density estimate. Not a present-day population estimate.

Original peer-reviewed descriptions remain the strongest morphology references when modern studies do not revisit diagnostic characters. Curated source accounts can support taxonomic, type locality and clearly labeled transcribed diagnosis data. No biological fact is invented to fill a blank account.

## Scientific provenance and status

The 96 species keys remain unchanged. The indexed bibliography now holds 245 distinct taxon–source links (four new unique paper/database links), classed by **Identification, Habitat, Distribution, Ecology, Conservation, Taxonomy, and Other**. Topic tags derive only from source-attached claim categories or explicitly taxonomic checklists, not from an assumed domain. There are still only **12 species with newly checked 2025–26 studies**, leaving 84 for individual current-publication research. Link counts do not measure how many primary papers have been reviewed.

## Field-guide UI

Inside **Sources & details → Filter references**, users can choose publication year (**All years; 2025–26; 2020–24; Before 2020; Year not listed**) and research topic. Both selectors apply together and the heading displays the current matching number of references. Three references appear first, with more behind Show more. Each has a source link. The filter controls stay collapsed until requested. Unknown source dates remain unknown rather than being inferred. No specialist gate or extra introductory text appears on the account card.

## Engineering gates

Source tests: `scripts/test_rc16_evidence_filters.py` plus all prior Python contracts. Android screenshot and basic filter disclosure checked by existing `Rc11SpeciesAccountInstrumentedTest`. Debug APK must retain original package, pinned signing certificate, 58 attributed offline JPG assets, versionCode at least 68, SQLite 27 and previous survey backup/upgrade behavior. Physical-phone QA remains open. Direct APK handoff must be verified before linking.
