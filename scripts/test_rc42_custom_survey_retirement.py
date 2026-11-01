"""Custom Survey retirement: public creation paths removed, legacy archives preserved."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"app/src/main/java/org/phwildlife/survey"

class CustomSurveyRetirement(unittest.TestCase):
    def test_only_three_supported_creation_tiles(self):
        src=(SRC/"WelcomeActivity.java").read_text(encoding="utf-8")
        for group in ("Birds","Herpetofauna","Mammals"):
            self.assertIn('surveyTile(',src)
            self.assertIn('()->setup("'+group+'")',src)
        self.assertNotIn('surveyTile(second,"Custom"',src)
        self.assertNotIn('private void customPicker()',src)
        self.assertNotIn('heading("Custom survey"',src)
        self.assertNotIn('putExtra("custom_survey"',src)
        self.assertNotIn('putBoolean("custom"',src)
        self.assertNotIn('setup("Birds",true)',src)
        self.assertNotIn('setup("Herpetofauna",true)',src)
        self.assertNotIn('setup("Mammals",true)',src)

    def test_old_navigation_falls_back_safely(self):
        src=(SRC/"WelcomeActivity.java").read_text(encoding="utf-8")
        self.assertIn('rememberedProfile.equals("Birds")',src)
        self.assertIn('rememberedProfile.equals("Herpetofauna")',src)
        self.assertIn('rememberedProfile.equals("Mammals")',src)
        self.assertIn('else home();',src)
        self.assertIn('fieldworkHub();',src)

    def test_no_database_migration_or_legacy_record_rewrite(self):
        db=(SRC/"FieldDb.java").read_text(encoding="utf-8")
        self.assertIn('super(context,NAME,null,27)',db)
        recorder=(SRC/"MainActivity.java").read_text(encoding="utf-8")
        self.assertIn('private void restoreEvent()',recorder)
        self.assertIn('survey_profile',recorder)
        self.assertIn('not_counted',recorder)
        history=(SRC/"HistoryActivity.java").read_text(encoding="utf-8")
        self.assertIn('SurveyOutcomeSql.RESOLVED',history)

if __name__=="__main__":
    unittest.main()
