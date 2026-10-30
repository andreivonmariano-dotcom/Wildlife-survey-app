"""RC52 screen 1: stable palette, accessible destinations, no duplicate hero label."""
from pathlib import Path
import re
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name): return (JAVA/name).read_text()

class Rc52HomeTheme(unittest.TestCase):
    def test_home_has_one_brand_heading_and_three_destinations(self):
        home=source("WelcomeActivity.java").split("private void home(){",1)[1].split("private void fieldworkHub(){",1)[0]
        self.assertNotIn('LikasPortalUi.kicker(this,"EXPLORE LIKAS")',home)
        self.assertEqual(home.count("LikasPortalUi.tile(this,"),3)
        self.assertIn("LikasPortalUi.session(this,()->",home)
        self.assertLess(home.index("LikasPortalUi.session(this,()->"),home.index('LikasPortalUi.tile(this,"Fieldwork"'))
        self.assertEqual(home.count("UiKit.LEAF_LIGHT,UiKit.LEAF_MID"),2)

    def test_theme_uses_shared_home_tokens_and_preserves_owner_icons(self):
        theme=source("LikasDesignSystem.java")
        portal=source("LikasPortalUi.java")
        for key in ("HOME_CARD_MIN_DP","HOME_ICON_DP","HOME_HERO_GAP_DP","TEXT_HOME_ACTION"):
            self.assertIn(key,theme)
            self.assertIn(key,portal if key!="HOME_HERO_GAP_DP" else source("WelcomeActivity.java"))
        self.assertIn('TextView mark=text(a,"LIKAS",',portal)
        self.assertIn("Explore. Record. Conserve.",portal)
        for res in ("likas_approved_fieldwork","likas_approved_species_library",
                    "likas_approved_settings","likas_natator_frog"):
            self.assertIn(res,portal)
        self.assertNotIn("cover.addView(contours",portal)
        self.assertIn("cover.setWillNotDraw(false)",portal)

    def test_same_package_schema_and_preview_version(self):
        build=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 113",build)
        self.assertIn("0.27.3-rc53-fieldwork-hub-preview",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,27)",source("FieldDb.java"))
if __name__=="__main__": unittest.main()
