"""Source-level safeguards for the LIKAS offline startup and survey flow."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"
RES = ROOT / "app/src/main/res"
ANDROID = "{http://schemas.android.com/apk/res/android}"


class LikasBrandingSmokeTest(unittest.TestCase):
    def test_adaptive_icon_has_real_foreground_vector(self):
        icon = ET.parse(RES / "mipmap-anydpi-v26/ic_launcher.xml").getroot()
        self.assertEqual(icon.tag, "adaptive-icon")
        self.assertEqual(
            icon.find("foreground").attrib[ANDROID + "drawable"],
            "@drawable/likas_icon_foreground",
        )
        fg = ET.parse(RES / "drawable/likas_icon_foreground.xml").getroot()
        self.assertEqual(fg.tag, "vector")
        self.assertGreaterEqual(len(fg.findall("path")), 3)

    def test_startup_is_offline_and_has_resume(self):
        manifest = ET.parse(RES.parent / "AndroidManifest.xml").getroot()
        application = manifest.find("application")
        self.assertEqual(application.attrib[ANDROID + "icon"], "@mipmap/ic_launcher")
        launch = (JAVA / "LikasLaunchActivity.java").read_text(encoding="utf-8")
        self.assertIn('new LikasForestView(this)', launch)
        self.assertIn('action("Resume Survey",true)', launch)
        self.assertIn('available?"Home & Tools":"New Survey"', launch)
        self.assertIn("available?View.VISIBLE:View.GONE", launch)

    def test_pixel_forest_bounded_in_memory_and_frame_rate(self):
        forest = (JAVA / "LikasForestView.java").read_text(encoding="utf-8")
        self.assertIn("PIXELS_W=216, PIXELS_H=384, FRAME_DELAY_MS=100", forest)
        self.assertIn("Bitmap.createBitmap(PIXELS_W,PIXELS_H,Bitmap.Config.ARGB_8888)", forest)
        self.assertIn("postInvalidateDelayed(FRAME_DELAY_MS)", forest)
        self.assertIn("LikasUiRules.daylightFactor(minute)", forest)
        self.assertIn("ink.setFilterBitmap(false)", forest)
        self.assertNotIn("WebView", forest)
        self.assertNotIn("requestLocationUpdates", forest)

    def test_survey_end_remains_available_to_observer(self):
        recording = (JAVA / "MainActivity.java").read_text(encoding="utf-8")
        self.assertIn('endSurveyButton=UiKit.button(this,"End Survey"', recording)
        self.assertIn('root.addView(endSurveyButton,endParams)', recording)
        for item in ("Finish Complete", "End Incomplete", "Cancel Survey"):
            self.assertIn(item, recording)
        self.assertIn("exportZip()", recording)


if __name__ == "__main__":
    unittest.main()
