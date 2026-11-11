"""APO coherent-shell contracts: navigation, visual consistency and data continuity."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name): return (JAVA/name).read_text(encoding="utf-8")

class ApoRebuildShell(unittest.TestCase):
    def test_four_accessible_persistent_targets(self):
        bar=source("ApoShellBar.java")
        welcome=source("WelcomeActivity.java")
        for tab in ("home","fieldwork","atlas","data"):
            self.assertIn('"'+tab+'"',bar)
        self.assertIn('item.setMaxLines(2)',bar)
        self.assertIn('item.setContentDescription',bar)
        self.assertIn('item.setFocusable(true)',bar)
        self.assertIn('item.setMinHeight',bar)
        self.assertIn('shellNavigation.addView(ApoShellBar.create',welcome)
        self.assertIn('shellNavigation.setVisibility(View.GONE)',welcome)
        self.assertIn('private void showShellPage(String page)',welcome)
        self.assertIn('showShellPage("fieldwork")',welcome)
        self.assertIn('showShellPage("data")',welcome)

    def test_home_has_one_primary_survey_and_real_reference_routes(self):
        welcome=source("WelcomeActivity.java")
        home=welcome.split('private void home(){',1)[1].split('private void bioToolsHub(){',1)[0]
        self.assertIn('ApoHomeUi.primary(this,"Start Survey"',home)
        self.assertIn('ApoHomeUi.resume(this,',home)
        self.assertIn('this::openAtlas',home)
        self.assertIn('this::openResearch',home)
        self.assertEqual(home.count('ApoHomeUi.module(this,'),2)
        self.assertIn('ApoHomeUi.preview(this,"Map"',home)
        self.assertNotIn('this::bioToolsHub',home)
        self.assertNotIn('phMapsHub()',welcome)
        self.assertIn('PhLibraryBibliographyActivity.class',welcome)
        self.assertIn('LikasLibraryActivity.class',welcome)

    def test_one_data_workspace_and_unmodified_survey_outcomes(self):
        welcome=source("WelcomeActivity.java")
        data=welcome.split('private void myDataHub(){',1)[1].split('private void fieldworkHub(){',1)[0]
        self.assertEqual(data.count('ApoHomeUi.tool(this,'),6)
        self.assertIn('HistoryActivity.class',data)
        self.assertIn('FieldBackupActivity.class',data)
        self.assertIn('return surveys+" saved surveys · "+observations+" observations"',welcome)
        recorder=source("MainActivity.java")
        for value in ('"End Survey"','"Save Observation"',
                      'SurveyClosePolicy.resolve(', '"Cancel Survey · Not Counted"'):
            self.assertIn(value,recorder)
        self.assertIn('super(context,NAME,null,29)',source("FieldDb.java"))

    def test_shared_theme_and_original_identity(self):
        visual=source("ApoVisualSystem.java")
        self.assertIn('FIELD_BACKGROUND',visual)
        self.assertIn('READING_BACKGROUND',visual)
        self.assertIn('ApoVisualSystem.FIELD_BACKGROUND',source("UiKit.java"))
        self.assertIn('ApoVisualSystem.READING_BACKGROUND',source("PhLibrary2Ui.java"))
        self.assertIn('R.drawable.apo_splash_mark',source("ApoHomeUi.java"))
        gradle=(ROOT/"app/build.gradle").read_text(encoding="utf-8")
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("0.27.4-rc",gradle)

if __name__=="__main__":
    unittest.main()
