# LIKAS Greater Mindanao PAIC | v3.3 private source-evidence corrections

**31 October 2026 (Asia/Manila). Private research edition; no Android release.**

## Owner scope
The project owner confirms that the intended LIKAS APK is **solely for their personal noncommercial consumption and will not be distributed**. This instruction narrows the planned delivery, but is not a transferable public redistribution license for WBCP, Sy, Avibase, or other compiled datasets. Store the full owner-derived v3.3 workbook and any import JSON in owner-controlled private files, not the public GitHub source tree, public CI artifacts or publicly available APK. Code/docs without protected source tables may remain public.

## Verified private curation deliverable
- Source: owner-uploaded v3.2.0 XLSX, SHA-256 `ae66c2278ef9da8d96ff6e05a65d69802b0eaf592ef6517d569049b7446220fc` (unchanged).
- v3.3.0 corrected private XLSX SHA-256 `964a6a6b6fa1ba282bff3cf788062a360a9db04603d7b3b5e6e8a2f6a92d11a3`.
- 721 INCLUDED named vertebrate candidates: birds 413, reptiles 156, amphibians 53, mammals 99; one explicitly EXCLUDED candidate retained; zero pending region decision rows.
- 1,327 distinct taxon/island evidence rows and 880 source-assertion rows. Unique taxon IDs and names, master/included parity, source assertion/evidence relationships, source URLs and archive integrity were checked.
- The corrected private workbook adds a full previous/new-value change log (395 cell/row actions), explicit outstanding verification, and a source-rights register.
- Directly supplied private exports in the owner's ChatGPT conversation: XLSX, CSV/JSON research package and QA report. Chat attachments are not assumed durable across sessions.

## Source-grounded corrections
1. *Pseudorabdion mcnamarae*: Sy 2026-1 lists Biliran among Philippine islands, not Mindanao. Corrected the false Davao Oriental/Avibase text and retained only Biliran as source-supported PAIC island. Original source distribution is preserved in the source-assertion record.
2. *Gekko mindorensis*: Sy 2026-1 names Camiguin Sur (not mainland Mindanao). Sanguila et al. (2016, ZooKeys 624, doi:10.3897/zookeys.624.9814) identified the Camiguin Sur/Panglao material as *G. cf. mindorensis* pending taxonomic resolution. Corrected source false Davao Oriental attribution and explicitly recorded associated oceanic Camiguin Sur with species-concept caution; do not promote mainland Mindanao broad database claims as voucher-confirmed identity.
3. *Brachymeles libayani*: Replaced erroneous Bohol Island evidence with Lapinig Chico, Lapinig Grande, Tilmubo, Tintiman and Polong Dako (small islands of Bohol Province). No assumption of Bohol Island occurrence or prehistoric land-bridge link.
4. Taxon crosscheck now differentiates 597 exact names; 19 subspecies-level names; one older combination for *Indosylvirana nicobariensis*; three later *Platymantis* concepts; one current reptile combination not under that name in 2026 Sy; one WBCP-unmatched *Pachyglossa agilis*; 99 mammal national MDD source-file comparisons **not independently evaluated**; one already-flagged bird taxon concept. Previously the last 124 were misleadingly labelled “NOT IN NATIONAL VERSION.” Mammal source file was not supplied, so do not claim reconciliation is complete.
5. Four birds and ten mammals had missing occurrence/origin fields explicitly set to **NOT ASSESSED IN AVAILABLE SOURCE**; prior LIKELY/POSSIBLE geographic-support terms were removed from the biological occurrence fields of the two reptile cases. No inferred residency or origin added.
6. Sumilhig et al. (2024) publisher's article webpage labels its license **CC BY-NC 4.0**, while accessible PDF wording differs. Private source register records discrepancy, conservative license reading and DOI `10.54610/jeseg.v6i2.137`.

## Remaining scientific limits
- 81 Avibase-compiled regional bird cases are secondary source-supported candidates, not a completed independent dated-observation/voucher review.
- MDD v2.5 source file (99 mammals) and 2026 *Platymantis* concept crosswalk remain to be validated.
- *Gekko mindorensis* complex identity, land-bridge status of Bohol-associated small islands, and multiple literature/specimen cases remain under assessment.
- This is not a site-level occurrence inventory, current biological census, native resident-only list, or direct inference from the owner's GPS.

## Engineering/release boundary
No application code, APK, SQLite schema, historical observations, startup assets, signing identity or upgrade state changed in this research curation step. The existing `ChecklistPackImporter` expects a hash-pinned four-CSV national source ZIP, not v3.3 PAIC JSON; do not claim the private ZIP is currently importable by Android. Next app task: versioned user-selected read-only private PAIC reference import with verification before any schema/UI changes; preserve user observation data and device upgrade continuity.
