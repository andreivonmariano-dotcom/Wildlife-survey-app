# PHLibrary Book 01 | Locked app-content reference

**Owner-approved:** 8 November 2026 (Asia/Manila)  
**Status:** Approved canonical *editorial/content* baseline for the next PHLibrary Android data import. Not yet installed in an APK or committed as application binary assets.

## Canonical files

1. **Book text:** `PHLibrary_Book_01_Greater_Mindanao_v1.82_Final_Habitat_Language_715.docx`
   - SHA-256: `a35f0a53e0586a071c1bd9ff807150643313982503693cd06ea8905f7aeaa279`
   - Personal Library durable path: `/PHLibrary/Locked References/PHLibrary_Book_01_Greater_Mindanao_v1.82_Final_Habitat_Language_715.docx`
   - File Library record: `libfile_e9f7655710b48191aa322a019fe4551f`
   - Verified in current container: 715 `Heading 4`, 715 `Article Text`, 715 `Badge Strip` and 26 tables, with zero `associated with` in `Article Text`. Signed/source import is future work.
2. **Discovery Rarity companion:** `PHLibrary_Discovery_Rarity_v0.3.4_Taxonomic_Reconciliation_715.xlsx`
   - SHA-256: `2f0756f5a662e1ea33ac069cf3d7c805949d5568f164198807cb615c1ccd87cc`
   - Personal Library durable path: `/PHLibrary/Locked References/PHLibrary_Discovery_Rarity_v0.3.4_Taxonomic_Reconciliation_715.xlsx`
   - File Library record: `libfile_bb5f2bf09370819185796e47293efd14`

The file hashes were checked against the session's mounted Word and spreadsheet files **before** copying them to the persistent Library. Copy/upload calls both succeeded. The Project/GitHub repository has this manifest and not the original Word/XLSX bytes; later imports must retrieve and validate the correct files.

## Identity and rank contract

- Exactly **715 book accounts**, covering **409 bird accounts, 53 amphibian accounts (one provisional identification), 154 reptile accounts and 99 mammal accounts**.
- Rarity display distribution: **124 Common; 336 Uncommon; 214 Rare; 37 Exceptionally rare; 3 Legendary; 1 Unranked**. Thus 714 ranks assigned to 715 entries. Discovery Rarity remains an educational editorial estimate, with IUCN and DENR statuses independent.
- `Oreophryne cf. nana` is a **provisional Mindanao identification**, not a formal accepted species `Aphantophryne nana`. The latter is historical Camiguin material and a crosswalk source, not a synonym or inherited conservation status. Preserve the `cf.` label and Unranked status.
- `Ceyx melanurus` and `Ceyx mindanensis` are **two distinct Book 01 entries**. Historical records under the broad `C. melanurus` concept require a versioned species-concept crosswalk before assigning them to the two new records. They are Rare and Uncommon, respectively, in the book's educational rarity classifications.
- Keep the exact long **one-paragraph species narrative**, taxonomic group, authored common name, status metadata, bibliographic references and original historical aliases. Habitat interpretations in this educational book are editorial synthesis; they must not become recorded survey observations, range confirmations or measured selection.
- Approved app-facing PHLibrary style: **text-only, icon-free, photo-free**, restrained typography, one prominent Book 01 entrance, readable single uninterrupted narrative, quiet source access and accessible enlarged text. Do not restore previously removed photo and icon-heavy cards without new owner approval.

## Source-of-truth and import gate

Book v1.82 supersedes Book v1.75 and app/editorial overlay versions v1.76–v1.80 **for future reader content**. Prior files are historical evidence, not the new app-visible default. This decision does not change current RC100 Android code/asset data. Do not overwrite existing user survey records, field identifications or original PAIC714 taxonomy IDs.

Before importing:
1. Convert the canonical Word into a **single generated, versioned offline content package**, with a reproducible parser, stable scientific/taxon concept IDs, alias mapping, reference links, text fidelity checks and edition/checksum manifest. Do not manually transcribe 715 narratives.
2. Validate counts, 715 exact keyed accounts, one unranked `cf.` record, both `Ceyx` taxa, source/citation links, Unicode, original text and no missing/duplicated names. Ensure alias mappings are one-to-one only when evidenced; old `Ceyx melanurus` records must not automatically fan out into both species.
3. Reader source/version verification, import failures, data provenance, font-scale search and field-data upgrade regression gates must pass on the candidate app branch. Preserve package ID, signer and SQLite29 compatibility.
4. Only a separately built and verified signed APK can be described as containing Book v1.82.

## Relationship to audit proposal

See `docs/APO_PHLIBRARY_CURRENT_STATE_AUDIT_AND_REDESIGN_2026_11_08.md`. The **reference lock is accepted** by the owner; interface redesign proposals in that document are **recommendations pending separate approval**. Do not implement redesign based only on this audit file.
