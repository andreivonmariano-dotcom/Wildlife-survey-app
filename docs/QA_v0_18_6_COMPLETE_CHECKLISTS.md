# LIKAS v0.18.6 private wildlife checklist and survey-end QA

## Existing data and source license
- Back up the device's field survey ZIP before installing. Preserve existing
  package ID and matching signing key. Do not uninstall the field application.
- The WBCP 2025 Philippine bird checklist is provided for **personal or
  scientific reference only**, with original WBCP citation and copyright.
- Emerson Y. Sy amphibian 2025-1 and reptile 2026-1 sources were uploaded
  by the researcher. Redistribution permission has not been confirmed.
- No copyrighted species rows, photographs or audio are embedded in this
  APK or GitHub branch. A separate user-only ZIP pack contains the
  researcher-supplied CSV derivatives for private field use.
- Original field observations and their taxonomy provenance must never be
  renamed or rewritten by checklist updates.

## Combined import package
- The user selects **Data & Tools > Offline taxonomy >
  Install complete Philippine checklists (ZIP)**.
- Package structure:
  `birds.csv` (761 rows, WBCP v2025 IOC 15.1);
  `herpetofauna.csv` (116 Sy 2025-1 amphibians and 380 Sy 2026-1
  reptile entries);
  `bird_evidence.csv` (179 explicit Mindanao source mentions);
  `herpetofauna_evidence.csv` (184 region rows: 173 Mindanao entries and
  11 explicit Davao-province mentions).
- Source CSV SHA-256 hashes are pinned in `ChecklistPackImporter`, which
  verifies the data before beginning a SQLite transaction.
- Check rejected ZIP, missing files, wrong versions, duplicate keys,
  corrupted bytes and malformed CSV. Neither the taxon catalog nor
  occurrence evidence changes after failure.
- Confirm exactly 1,257 source taxon rows and 363 region-evidence rows,
  alongside the already installed ASM MDD v2.5 mammal candidates.
- Re-import the package once. The number of source taxon keys and
  evidence keys must remain stable. Old saved survey rows must be intact.
- Test offline airplane mode. All names, distribution texts, status fields,
  and source-verified area evidence remain accessible.
- Check iucn and PH statuses are source snapshots, not an assertion
  about current reassessment.

## Science data constraints
- A source-listing for Mindanao or Davao is not a station sighting.
- An unmentioned species is unknown for that geographic scope, not
  necessarily absent from the wild.
- WBCP "Mindanao?" uncertain record is not in the source-listed
  island-evidence file. Davao note-only candidates pending verification
  are excluded from the accepted province list.
- The unassigned `Ophiophagus` record remains genus-level and is not
  given an invented binomial.
- Ensure matching historical synonyms offer accepted taxonomic names
  without altering saved verbatim observations.
- Reopen a survey with old scientific names, then export and check
  those exact original names persist.

## Completion review and status archive
- Tap permanently visible End Survey from every wildlife method.
- Choose Finish Complete. Review station count, completed visits, active
  effort, route measured length and route-completion confirmation before
  saving. Missing sampling targets classify as incomplete.
- Choose End Incomplete. Enter an optional reason. Verify
  `survey_outcome_audit.reason` is populated with the researcher's note
  or default incomplete classification explanation.
- Choose Cancel Survey and enter at least five characters explaining
  exclusion. Outcome must be `not_counted` with saved records retained.
- Survey History's outcome filter correctly selects Complete, Incomplete,
  Canceled and Active surveys without duplicating archived results.
- Opening an incomplete or canceled record shows outcome reason, audit
  time and the observation count at closure.
- Field dashboard analysis includes `complete` only. The regular backup
  ZIP retains all records and the survey outcome audit.

## Release checks
- GitHub Actions: Python checklist validation and
  `:app:testDebugUnitTest :app:assembleDebug`.
- Verify update certificate matches the previous LIKAS beta APK.
- Record the outcome of a 60-minute field test on at least one handset:
  fresh GPS, 9-point route, timed observation, incomplete survey,
  canceled survey, complete survey, and in-place upgrade from v0.18.5.
- Public app release requires review of WBCP/Sy redistribution terms.
