"""RC57 Home 200% font layout contract; real bounds are checked on Android emulator."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
class Rc57Home(unittest.TestCase):
    def test_large_font_hero_is_scoped_and_original_art_retained(self):
        source=(ROOT/"app/src/main/java/org/phwildlife/survey/LikasPortalUi.java").read_text()
        self.assertIn("final boolean largeHomeText=!compact",source)
        self.assertIn("fontScale>=1.60f",source)
        self.assertIn("largeHomeText?118:151",source)
        self.assertIn("largeHomeText?21:26",source)
        self.assertIn('"Philippine wildlife"',source)
        self.assertIn('"Explore. Record. Conserve."',source)
        self.assertIn("likas_natator_frog",source)
        self.assertIn("likas_approved_fieldwork",source)
    def test_same_identity_next_upgrade_code(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 120",gradle)
        self.assertIn("0.27.3-rc60-library-visual-preview",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        db=(ROOT/"app/src/main/java/org/phwildlife/survey/FieldDb.java").read_text()
        self.assertIn("super(context,NAME,null,27)",db)
    def test_no_home_actions_removed(self):
        source=(ROOT/"app/src/main/java/org/phwildlife/survey/WelcomeActivity.java").read_text()
        home=source.split("private void home(){",1)[1].split("private void fieldworkHub(){",1)[0]
        for action in ('"Fieldwork"','"Library"','"Settings"','LikasPortalUi.session(this,()->'):
            self.assertIn(action,home)
if __name__=="__main__":unittest.main()
