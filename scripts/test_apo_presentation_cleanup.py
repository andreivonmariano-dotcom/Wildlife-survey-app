"""Presentation contracts: concise copy, preserved scientific and safety meaning."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(name): return (JAVA/name).read_text(encoding="utf-8")

class ApoPresentationCleanup(unittest.TestCase):
    def test_data_workspace_groups_all_existing_routes(self):
        data=src("WelcomeActivity.java").split("private void myDataHub(){",1)[1].split("private void fieldworkHub(){",1)[0]
        for title in ("Records","Tools","Data protection"):
            self.assertEqual(data.count('ApoHomeUi.section(this,"'+title+'"'),1)
        self.assertEqual(data.count("ApoHomeUi.tool(this,"),6)
        for route in ("HistoryActivity.class","SitesActivity.class","HabitatRecordsActivity.class","this::analysisHub","this::fieldUtilitiesHub","FieldBackupActivity.class"):
            self.assertIn(route,data)
    def test_map_keeps_provenance_and_readonly_access(self):
        map_ui=src("ApoOfflineMapActivity.java")
        for marker in ("SQLiteDatabase.OPEN_READONLY","Survey-start GPS fix","Saved site reference coordinate","WGS84 (EPSG:4326)","low-resolution simplified polygons","faeldon/philippines-json-maps","without a distance scale"):
            self.assertIn(marker,map_ui)
        self.assertIn('legend.setOnClickListener(',map_ui)
        self.assertNotIn('Filter · All records · All projects',map_ui)
    def test_research_keeps_reference_uncertainty_and_source(self):
        bib=src("PhLibraryBibliographyActivity.java")
        for marker in ("Citation only","Not verified","sourceUrl.startsWith","Open online source","ApoReferenceBookmarks.setSaved"):
            self.assertIn(marker,bib)
        self.assertNotIn("Full text: unavailable in this offline edition.",bib)
    def test_backup_keeps_privacy_and_empty_workspace_guards(self):
        backup=src("FieldBackupActivity.java")
        for marker in ("Keep this ZIP private","may contain personal information","Restore into an empty workspace","FieldBackupArchive.hasActiveSurvey","FieldBackupArchive.canRestore","FieldBackupArchive.restoreIntoEmpty","FieldBackupArchive.export"):
            self.assertIn(marker,backup)
        self.assertNotIn("No backup or restore has been run on this device.",backup)
    def test_survey_preserves_field_science(self):
        setup=src("WelcomeActivity.java")
        recorder=src("MainActivity.java")
        self.assertIn("Area selection does not confirm species presence.",setup)
        self.assertIn("GeoChecklistPolicy.canAutoSelect",setup)
        for marker in ("SurveyClosePolicy.resolve(","End Survey","Save Observation","Cancel Survey · Not Counted","Record effort separately."):
            self.assertIn(marker,recorder)
        self.assertIn("super(context,NAME,null,29)",src("FieldDb.java"))

if __name__=="__main__":unittest.main()
