# APO main integration and verification | 11 November 2026

**Task:** Promote the selected, signed/upgrade-tested RC112 source to default `main` with both independent histories preserved.

## Exact source lineage

- Historical main parent: `eb726a840b6c9014a271a2944d3d62e84679ca91`; previous Android source was v0.15 and governance documents were updated through 11 November 2026.
- Consolidated APO RC112 parent: `2bf3371fa30bfe7b267d02bbfb87af341d6409a4`, derived from signed RC112 `acddbb2b292ac9939383183d9b0100d2dc31945d`. It retained app ID `org.phwildlife.survey.beta08`, versionCode172, SQLite29, scientific data, survey records and all media hashes.
- The integration uses an explicit **two-parent merge commit**, with historical main as first parent and RC112 as second. No force push or history deletion is authorized.
- The resulting tree takes app code, tests, assets and two live workflows from the tested consolidated RC112 branch; includes every main-only historical scientific document; and moves the two old main workflows into `docs/archive/ci/`. Each original remains recoverable from its parent.
- Approved Charter v1.2 remains byte-identical to its main-parent version and canonical Google Doc. The owner approval rule for subsequent charter amendments is unchanged. The RC112 appendix-only project-state and decision log remains active, with the main parent's older independent log retained in history.

## Verification gates

- **Prior signed evidence:** run `42852787251` on source commit `85bfba7f7d00ec3da04fc058b98f827a57c51078` succeeded in scientific-source, Android, gallery, screen and signed RC109→RC112 emulator upgrade checks. Subsequent consolidation commits modified documents only.
- **Integration staging:** `integration/apo-main-rc112-20261111`. The quality workflow runs on both staging and `main` with the approved source and prior signing certificate.
- **Final QA state:** Staging Actions run `42856304093`, attempt **2**, **SUCCESS** on exact merge commit `157c268c1fb6fb2f015ca7ca5a4010ab3bbb3da8`. The earlier attempt had one Android emulator process-attachment failure after the signed APK and preceding emulator tests succeeded; the retry passed all tests and the signed RC109→RC112 no-clear-data upgrade. Default `main` fast-forwarded to that same commit without force, overwrites or history removal. Its own follow-up Actions run `42859195154` was initiated, with final outcome pending at this record's creation. Do not treat this report's creation as evidence of a passing final integration build.

## Release and permission boundaries

This is **source control integration** and an **owner-preview development baseline**. Public redistribution and formal release remain blocked by rights and durability questions about owner wildlife photographs ([#116](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/issues/116)), private-labelled references in the public repository ([#117](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/issues/117)), and the full scientific/reader-facing editorial QA gate ([#119](https://github.com/andreivonmariano-dotcom/Wildlife-survey-app/issues/119)). A signed APK and physical-phone acceptance must be verified separately.

Never rename the installed Android package, signer, historical DB schema, source taxon IDs, or user data as a consequence of source-branch integration.
