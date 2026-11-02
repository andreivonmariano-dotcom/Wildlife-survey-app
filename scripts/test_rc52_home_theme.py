"""RC52 screen 1: stable palette, accessible destinations, no duplicate hero label."""
from pathlib import Path
import re
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name): return (JAVA/name).read_text()

class Rc52HomeTheme(unittest.TestCase):
    def test_home_has_one_brand_heading_and_three_destinations(self):
        home=source("WelcomeActivity.java").split("private void home(){",1)[1].split("private void phMapsHub(){",1)[0]
        self.assertIn("ApoHomeUi.header(this,",home)
        self.assertEqual(home.count("ApoHomeUi.module(this,"),4)
        self.assertIn("ApoHomeUi.resume(this,",home)
        self.assertLess(home.index("ApoHomeUi.resume(this,"),
                        home.index('ApoHomeUi.module(this,"01","BioTools"'))
        self.assertNotIn("LikasPortalUi.hero(",home)
        self.assertNotIn("LikasPortalUi.tile(",home)
    def test_theme_uses_shared_home_tokens_and_preserves_owner_icons(self):
        theme=source("LikasDesignSystem.java")
        portal=source("LikasPortalUi.java")
        apo=source("ApoHomeUi.java")
        for key in ("HOME_CARD_MIN_DP","HOME_ICON_DP","HOME_HERO_GAP_DP","TEXT_HOME_ACTION"):
            self.assertIn(key,theme)
        for res in ("likas_approved_fieldwork","likas_approved_species_library",
                    "likas_approved_settings","likas_natator_frog"):
            self.assertIn(res,portal)
        self.assertNotIn("R.drawable.",apo)
        self.assertNotIn("ImageView",apo)
        self.assertIn("setFocusable(true)",apo)
        self.assertIn("setContentDescription",apo)
        self.assertNotIn("cover.addView(contours",portal)
    def test_same_package_schema_and_preview_version(self):
        build=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(build.split("versionCode ",1)[1].split()[0]),125)
        self.assertIn("versionName \'0.27.3-",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,29)",source("FieldDb.java"))
if __name__=="__main__": unittest.main()
