# LIKAS RC6 | Navigation, local recovery, Philippine wildlife references

**Prepared:** 28 October 2026, Asia/Manila
**Workstream:** draft PR #38, RC4-derived `feature/likas-core-fieldwork-post-rc4`
**Version:** `0.27.3-rc6-recovery-library-preview`, versionCode 51
**Package:** `org.phwildlife.survey.beta08`; SQLite schema 27 unchanged
**Requirements:** G-01 G-02 G-03, scientific survey lifecycle S, source taxonomy L and T, evidence/data D, UI U, compatibility C, test E.

## Q1: Navigation

The common `UiKit.navigationHeader` is used on Dashboard, History, Scientific Data Review, Projects/Sites, Station Progress, Study Templates, Field Protocols, Trap/Camera Records, Catalog, Regional Source Evidence, Species Reference/Picker, Habitat & Ecology, Data Management, Legacy Import, Sound Library and Diagnostic Comparison. The Field Protocol and Sound Library back actions retain original in-page navigation and unfinished-recording protection. The recorder, camera and GPS acquisition screens intentionally retain context-specific controls. Existing survey storage and protocol logic were not modified by this navigation pass.

## Q2: Current-version recovery preview

New components: `FieldBackupArchive` and `FieldBackupActivity`, registered in AndroidManifest and linked from Home. The separate v0.7 CSV importer is explicitly labeled legacy.

**Backup algorithm:** Refuse backup during an active survey. Use `PRAGMA quick_check` and a SQLite online `VACUUM INTO` snapshot, available only with underlying SQLite 3.27+. Verify that copy as a SQLite schema-27 database. Place the database, private observation photos in `record_media/`, acoustic files in `acoustic/`, and app-owned shared preference XML files into a ZIP with a manifest and SHA-256 for each entry. Apply per-entry, whole-archive, file-count, and safe-name limits. The exported ZIP can contain personal information and sensitive wildlife coordinates; it is not encrypted.

**Restore algorithm:** Only a current-format ZIP with matching hashes and valid SQLite quick-check/schema can be imported. Restore is blocked for any survey/observation, custom project, station, route, template, favorite, orphan private media or pending WAL. Restore never merges into an existing workspace. Retain a before-restore copy of the old *empty* database. Require a process restart to flush cached data. This is a development recovery tool requiring physical Android round-trip and rollback testing; no stable restore guarantee is claimed from source inspection.

**Risk/compatibility:** Android devices with SQLite older than 3.27 will refuse snapshot creation, not silently produce an inconsistent database file. This source branch has no database migration. Scientific research ZIP remains a separate non-restorable export. Real upgrade data retention and recovery are not proven by GitHub CI.

## Q3: Source-supported account expansion

Introductory accounts grow from 13 to **17**, covering all four groups in this iteration:
- Bird: `Dicaeum quadricolor` (Cebu flowerpecker). BirdLife 2021 species assessment and IUCN description.
- Amphibian: `Platymantis insulatus` (Gigantes limestone frog). AMNH Amphibian Species of the World, amphibian conservation needs 2020, and Alcala et al. FrogLog study.
- Reptile: `Varanus olivaceus` (Gray's monitor). IUCN SSC Monitor Lizard Specialist Group and CITES identification guide.
- Mammal: `Bubalus mindorensis` (tamaraw). DENR 2025 action plan, IUCN SSC Asian Wild Cattle Specialist Group, ZSL, and ASM Mammal Diversity Database.

Profiles provide observed-morphology guidance, dated sources, habitats, uncertainty, encounter recording advice, and limits. Neither a global conservation category nor country-level listing establishes site presence. The reference images are unchanged from RC5 (10 real bundled images in the earlier tested APK), and **no new licensed photographs** were claimed for these four accounts. All account data remains subject to expert identification review and licensing checks.

## Mandatory acceptance gate before stable field release

1. Test old RC4 → RC5 → RC6 over-install with the same signer and package, preserving SQLite 27 records, local media, project IDs, route IDs, and zero-detection effort.
2. On a sacrificial real Android device with supported SQLite: create an archived complete survey with photo/audio, export full backup, inspect ZIP manifest, reinstall into a fresh empty workspace, restore, reopen LIKAS, compare exact records/media and test research ZIP output. Repeat with malformed and truncated archives and existing field data to verify refusal.
3. Review 320dp width, TalkBack, text scaling, keyboard, permissions, back stack, Home navigation and outdoor display contrast on all updated screens.
4. Until complete, retain PR #38 as draft and treat the APK as a **development preview**. Permanent data deletion must not be promoted on the sole strength of source tests.
