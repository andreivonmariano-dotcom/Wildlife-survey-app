# LIKAS editorial and science policy | Publication-first species accounts

**Effective:** 29 October 2026  
**Owner decision:** Q1 and Q2 accepted. No specialist approval, signatures, review queues, or expert certification gate for species reference accounts. This replaces the specialist-signoff plan in RC12 and earlier design notes.

## Evidence standard

The species library is a literature-based reference, not a taxonomic determination service. Include biological statements supported by published original species descriptions, taxonomic revisions, and peer-reviewed research on Philippine wildlife. Preserve relevant authoritative dated checklists for nomenclature and official dated status documents for Philippine DENR or IUCN conservation categories, with their separate source type and assessment year. Do not use an unattributed summary as an original peer-reviewed diagnosis. Source collections and conservation websites can remain as historical references, but prioritize the actual published study where available.

Each new claim stores the name, study title/authors, year, source link, section/page/table, short plain-language finding, source type and study-specific bounds. Preserve taxonomic disagreements. Keep the published sample locality separate from a taxon-wide range and a recorded field occurrence.

Never fabricate an unknown trait or automatically convert missing research to an asserted absence. Do not claim a full field identification key solely from one published character. No specialist gate, signature, credential, certification, or named reviewer is needed for LIKAS editorial completion. Publication provenance and factual correctness remain mandatory.

## Copy and visual rules for every user-visible screen

- Write like a concise Philippine wildlife field guide. Direct description, measured traits, ecology, published island distribution and dated conservation facts belong on the card.
- Remove repetitive AI-style disclaimers, defensive endings, editorial filler, release-candidate names, jargon, generic placeholders, and the phrase "specialist approval pending".
- Show no empty science cards. Display no "unassessed" inventory on the main species account. Use **Bibliography** and **Literature coverage** inside a collapsed source drawer.
- Preserve meaningful limits in the study details, including small sample sizes, collection sites, date-specific conservation categories, conflicting island records and uncertain taxonomy. Keep genuinely important safety warnings for venomous fauna and crocodiles.
- Do not erase the original bibliographic record to shorten a sentence. Edit only the presentation, or add a short curated paraphrase with its source.
- Every public card description should add a concrete species fact. A sentence whose only purpose is to defend the app's methodology belongs in technical documentation, not the field guide.

## Approved reference websites by evidentiary purpose

Official and curated sources complement, rather than replace, original scientific papers. The application ships a machine-readable source catalog at `app/src/main/assets/likas_scientific_reference_sources_v1.json` (10 vetted entries).

| Wildlife topic | Preferred websites | Safe use |
|---|---|---|
| Mammal classification | American Society of Mammalogists Mammal Diversity Database, `https://www.mammaldiversity.org/` | Accepted names, synonyms, original authorities, country distribution and revision citations. MDD v2.5, released 28 July 2026 |
| Mammal morphology and ecology | ASM `Mammalian Species` journal, `https://www.mammalsociety.org/publications/mammalian-species` | Individual published research-based species treatments and measurements with publication dates |
| Philippine mammal distribution and ecology | Field Museum Synopsis, `https://www.fieldmuseum.org/project-narrative/science/synopsis-of-philippine-mammals`, and Philippine Mammal Project, `https://www.philippinemammalproject.com/research.html` | Philippine specimen history, historic island occurrence, original descriptions and field papers. The online Synopsis' last major revision was 2010, so modern publications override it |
| Bats | Bat Conservation International, `https://www.batcon.org/about-bats/bat-profiles/` | Natural-history overview; follow the linked research for quantitative claims |
| Amphibian accounts | UC Berkeley-led AmphibiaWeb, `https://amphibiaweb.org/` | Habitat, calls, natural history and reference links |
| Amphibian taxonomy | American Museum of Natural History, `https://amphibiansoftheworld.amnh.org/` | Scientific names, synonyms, original descriptions and taxonomic history; record the access date for its live version 6.2 |
| Reptile taxonomy | The Reptile Database, `https://www.reptile-database.org/` | Valid names, descriptions, type localities, taxonomic changes, publication links; original paper is preferred for identification detail |
| Global conservation | IUCN Red List, `https://www.iucnredlist.org/` | Exact species-level IUCN assessment and year, not an undated or Philippine legal conservation category |
| Philippine conservation status | Original published Philippine DENR administrative orders and annexes | National legal status, with order number and effective date distinct from IUCN global category |
| General natural history cross-check | University of Michigan Animal Diversity Web, `https://animaldiversity.org/` | Supporting context only, as many pages are student-authored; not sole authority for diagnosis or changed conservation status |

Do not treat the words "specialist" in a legitimate institution's name as a demand for specialist sign-off. Curated online scientific entries are references, not peer-reviewed publications. Record `sourceType` and `retrievalDate` separately, and follow the original DOI for high-stakes morphology or taxonomic conflicts. Reproduce neither database prose nor images without their permitted usage rights.

## Working research inventory

- Full priority registry: **96**, unchanged, 24 birds + 24 amphibians + 24 reptiles + 24 mammals. The owner's earlier 92 count has no matching recorded source-defined subset.
- Historical RC9 twelve-field coverage is preserved as a source snapshot, not the current publication standard. Its obsolete expert-signoff field is omitted from the **RC13 eleven-field publication ledger** `app/src/main/assets/rc13_publication_coverage_v1.json`.
- First publication-focused batch `rc13_peer_reviewed_batch1_v1.json` adds **15** claims for `Platymantis paengi`, `P. rabori`, `Sarcophanops samarensis`, `Crateromys heaneyi`, and `Naja samarensis`. Sources: Siler et al. 2007 *Herpetologica*, Sanguila et al. 2016 *ZooKeys*, Campillo et al. 2020 *Biological Journal of the Linnean Society*, Gonzales & Kennedy 1996 *Journal of Mammalogy*, and Caasi et al. 2024 *Reptiles & Amphibians*.
- The tentative Sibuyan `P. paengi` distribution claim was omitted since the summary of the 2022 survey did not independently verify that exact record. The paper remains a useful reference, not a cited claim in this batch.
- RC12 published-source work remains attributed in the archived source files; its past specialist-gate UI has been superseded by **Literature coverage**.

## Consistency tests and release standards

`scripts/test_rc13_publication_policy.py` checks all 96 source names, exactly 11 publication topics, 15 new peer-reviewed statements, reference links, absence of any new expert-gate fields, source-sensitive UI text, and preservation of old evidence files. CI must compile Android and package the approved media and original signing certificate. Emulator tests must confirm the new reference drawer and unchanged live survey/backup flow. Physical-handset acceptance remains separate from scientific account research.
