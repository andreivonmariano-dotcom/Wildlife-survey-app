"""RC52 screen 1: stable palette, accessible destinations, no duplicate hero label."""
from pathlib import Path
import re
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name): return (JAVA/name).read_text()

class Rc52HomeTheme(unittest.TestCase):
    def test_home_has_one_primary_action_and_two_reference_routes(self):
        home=source("WelcomeActivity.java").split("private void home(){",1)[1].split("private void bioToolsHub(){",1)[0]
        self.assertIn("ApoHomeUi.header(this,",home)
        self.assertEqual(home.count("ApoHomeUi.module(this,"),2)
        self.assertEqual(home.count("ApoHomeUi.tool(this,"),0)
        self.assertIn('"Offline map is in development"',home)
        self.assertIn('ApoHomeUi.primary(this,"Start Survey"',home)
        self.assertIn("ApoHomeUi.resume(this,",home)
        self.assertLess(home.index("ApoHomeUi.resume(this,"),
                        home.index('ApoHomeUi.module(this,"01","Living Atlas"'))
        self.assertNotIn("LikasPortalUi.hero(",home)
        self.assertNotIn("LikasPortalUi.tile(",home)
    def test_theme_uses_shared_tokens_and_preserves_owner_icon(self):
        theme=source("LikasDesignSystem.java")
        portal=source("LikasPortalUi.java")
        apo=source("ApoHomeUi.java")
        visual=source("ApoVisualSystem.java")
        for key in ("HOME_CARD_MIN_DP","HOME_ICON_DP","HOME_HERO_GAP_DP","TEXT_HOME_ACTION"):
            self.assertIn(key,theme)
        for res in ("likas_approved_fieldwork","likas_approved_species_library",
                    "likas_approved_settings","likas_natator_frog"):
            self.assertIn(res,portal)
        self.assertIn("R.drawable.apo_splash_mark",apo)
        self.assertIn("ApoVisualSystem.FIELD_BACKGROUND",source("UiKit.java"))
        self.assertIn("ApoVisualSystem.READING_BACKGROUND",source("PhLibrary2Ui.java"))
        self.assertIn("public static final int FOREST",visual)
        self.assertIn("setFocusable(true)",apo)
        self.assertIn("setContentDescription",apo)

    def test_same_package_schema_and_preview_version(self):
        build=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(build.split("versionCode ",1)[1].split()[0]),125)
        self.assertIn("versionName \'0.27.4-",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,29)",source("FieldDb.java"))
if __name__=="__main__": unittest.main()
