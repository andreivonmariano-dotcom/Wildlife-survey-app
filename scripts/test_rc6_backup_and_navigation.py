"""Guard the experimental current-version LIKAS recovery and navigation design."""
from pathlib import Path
import json
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"
APP = (ROOT / "app/build.gradle").read_text()

class Rc6RestoreAndNavigationContracts(unittest.TestCase):
    def test_schema_lineage_and_version(self):
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'", APP)
        self.assertGreaterEqual(int(re.search(r"versionCode\s+(\d+)", APP).group(1)), 59)
        self.assertRegex(APP, r"versionName '0[.][0-9]+[.][0-9]+-rc[0-9]+[^']*'")
        self.assertIn("super(context,NAME,null,27)",(JAVA / "FieldDb.java").read_text())

    def test_backup_is_complete_and_recovery_does_not_merge(self):
        src = (JAVA / "FieldBackupArchive.java").read_text()
        for term in ("VACUUM INTO","PRAGMA quick_check",
                     "db/wildlife_field_v03.db","media/record_media/",
                     "media/acoustic/","prefs/",
                     "MAX_ARCHIVE","MAX_ENTRIES","checksum(",
                     "getCanonicalPath()","canRestore(ctx)",
                     "Restore is blocked: this workspace has saved field data",
                     "FieldDb.NAME+\".before-restore\""):
            self.assertIn(term,src)
        self.assertNotIn("getExternalStorageDirectory",src)

    def test_ui_has_separate_modern_restore_and_old_import(self):
        screen = (JAVA / "FieldBackupActivity.java").read_text()
        home = (JAVA / "WelcomeActivity.java").read_text()
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        self.assertIn("ACTION_CREATE_DOCUMENT",screen)
        self.assertIn("ACTION_OPEN_DOCUMENT",screen)
        self.assertIn("FieldBackupArchive.canRestore(this)",screen)
        self.assertIn("FieldBackupActivity.class",home)
        self.assertIn("Import older v0.7 export",home)
        self.assertIn('android:name=".FieldBackupActivity"',manifest)
        self.assertIn("v0.7 CSV export",(JAVA/"ImportActivity.java").read_text())

    def test_shared_navigation_preserves_recording_and_history(self):
        ui=(JAVA/"UiKit.java").read_text()
        self.assertIn("navigationHeader(Activity a",ui)
        for activity in ("AuditActivity","SitesActivity","DashboardActivity",
                         "HistoryActivity","CatalogActivity","HabitatRecordsActivity",
                         "DataManagementActivity","RegionalChecklistActivity",
                         "StudyProtocolsActivity","StationProgressActivity",
                         "TaxonDetailActivity","CallLibraryActivity",
                         "SpeciesPickerActivity","FieldProtocolsActivity",
                         "TrapProtocolActivity"):
            self.assertIn("UiKit.navigationHeader(this",(JAVA/(activity+".java")).read_text())
        self.assertIn("Stop recording and finish saving before leaving",
                      (JAVA/"CallLibraryActivity.java").read_text())
        self.assertIn("eventFilter=null;surveyOffset=0;observationOffset=0;draw();",
                      (JAVA/"HistoryActivity.java").read_text())
    def test_three_credible_and_distinct_new_species(self):
        pack=json.loads((ROOT/"app/src/main/assets/diagnostic_profiles_v1.json").read_text())
        self.assertEqual(17,len(pack["profiles"]))
        sources={s["id"] for s in pack["sourceList"]}
        for name in ("Dicaeum quadricolor","Platymantis insulatus","Varanus olivaceus","Bubalus mindorensis"):
            account=next(x for x in pack["profiles"] if x["scientificName"]==name)
            self.assertTrue(account["assessment"])
            for trait in account["traits"]:
                self.assertGreater(len(trait["value"]),30)
                self.assertTrue(set(trait["sourceIds"]) <= sources)
            self.assertTrue(set(account["account"]["sourceIds"])<=sources)
            self.assertTrue("DENR" in account["account"]["cautions"] or "national" in account["account"]["cautions"])

if __name__=="__main__":unittest.main()
