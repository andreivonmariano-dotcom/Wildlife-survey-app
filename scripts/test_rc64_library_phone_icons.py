"""Real-phone-inspired Library 64px/512px rendering and identity contract."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
class Rc64PhoneIcons(unittest.TestCase):
    def test_only_four_library_symbols_force_scale_to_plate(self):
        portal=(JAVA/"LikasPortalUi.java").read_text()
        for symbol in ("library-conservation","library-habitat-links","library-photo","library-sources"):
            self.assertIn('"'+symbol+'".equals(symbol)',portal)
        self.assertIn("ImageView.ScaleType.FIT_CENTER:ImageView.ScaleType.CENTER_INSIDE",portal)
        self.assertIn("art=image;size=approvedDp",portal)
        self.assertNotIn("Math.min(approvedDp,40)",portal)
        self.assertIn("setFilterBitmap(false)",portal)
        self.assertIn("frame.setClipToOutline(true)",portal)
    def test_original_assets_and_navigation_still_present(self):
        library=(JAVA/"LikasLibraryActivity.java").read_text()
        for row in ('"Conservation","Dated assessments","library-conservation"',
                    '"Habitat links","Cited associations","library-habitat-links"',
                    '"Photos","Offline images","library-photo"',
                    '"Sources","Checklists","library-sources"'):
            self.assertIn(row,library)
        self.assertEqual(18,len(list((ROOT/"app/src/main/res/drawable-nodpi").glob("likas_approved_*.png"))))
        build=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(build.split("versionCode ",1)[1].split()[0]),125)
        self.assertIn("0.27.3-rc65-library-optical-icons-preview",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,27)",(JAVA/"FieldDb.java").read_text())
if __name__=="__main__":unittest.main()
