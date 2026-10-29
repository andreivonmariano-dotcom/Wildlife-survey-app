"""Source-level safeguards for the LIKAS offline startup and survey flow."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"
RES = ROOT / "app/src/main/res"
ANDROID = "{http://schemas.android.com/apk/res/android}"


class LikasBrandingSmokeTest(unittest.TestCase):
    def test_adaptive_icon_uses_minimalist_frog_artwork(self):
        icon = ET.parse(RES / "mipmap-anydpi-v26/ic_launcher.xml").getroot()
        self.assertEqual(icon.tag, "adaptive-icon")
        self.assertEqual(
            icon.find("foreground").attrib[ANDROID + "drawable"],
            "@drawable/likas_natator_frog",
        )
        self.assertTrue((RES / "drawable-nodpi/likas_frog_icon.webp").is_file())
        import hashlib
        frog=RES / "drawable-nodpi/likas_natator_frog.png"
        self.assertTrue(frog.is_file())
        self.assertEqual(hashlib.sha256(frog.read_bytes()).hexdigest(),
            "06fe6f4a517d3ec382f7b66974ca3906ddadcfad9a04a6232063deb66b98bd37")
        round_icon = ET.parse(RES / "mipmap-anydpi-v26/ic_launcher_round.xml").getroot()
        self.assertEqual(
            round_icon.find("foreground").attrib[ANDROID + "drawable"],
            "@drawable/likas_natator_frog",
        )
        palette = ET.parse(RES / "values/likas_colors.xml").getroot()
        entries = {item.attrib["name"]: item.text for item in palette.findall("color")}
        self.assertEqual(entries["likas_icon_background"], "#08271E")
        self.assertFalse((RES / "values/colors.xml").exists())

    def test_approved_binary_assets_match_the_signed_off_zip(self):
        import hashlib
        assets = {
            "drawable-nodpi/likas_launcher_art.webp":
                "ad9079af4b9dcac0a024410a1989e28012d2c98052d88e295c9dd693df2d33d5",
            "drawable-nodpi/likas_frog_icon.webp":
                "aba89d67d5b2b815d24fb9e8c9edc3767bcc41e58d9d430f218b5ab05b0bc5e3",
        }
        for name, expected in assets.items():
            self.assertEqual(hashlib.sha256((RES / name).read_bytes()).hexdigest(), expected)
        forest = ROOT / "app/src/main/assets/likas_forest"
        for name, expected in {
            "forest_day_hd.webp": "8b62eda698354db6d410964a342e5ccbe8cd284e3577a51fc72e4839dface76f",
            "forest_night_hd.webp": "a21568f109b863cb86a27b8dfae87aebfa656914252232b6c70fc0c95f1467af",
            "forest_day.webp": "d89e3707b5fdbb3c2317a678f77cb46caeb2754ad1893d46397f25ba9d6fad41",
            "forest_night.webp": "238e18a31c1ac5be631103fe4dd44d09aaddc39c54f469c7589baa392e53012b",
        }.items():
            self.assertEqual(hashlib.sha256((forest / name).read_bytes()).hexdigest(), expected)

    def test_minimal_startup_is_offline_and_home_preserves_resume(self):
        manifest=ET.parse(RES.parent/"AndroidManifest.xml").getroot()
        self.assertEqual(manifest.find("application").attrib[ANDROID+"icon"],
                         "@mipmap/ic_launcher")
        launch=(JAVA/"LikasLaunchActivity.java").read_text(encoding="utf-8")
        home=(JAVA/"WelcomeActivity.java").read_text(encoding="utf-8")
        self.assertIn('brand.setText("LIKAS")',launch)
        self.assertIn('new Intent(this,WelcomeActivity.class)',launch)
        self.assertIn('ValueAnimator.areAnimatorsEnabled()',launch)
        self.assertIn('home.putExtra("show_menu",true)',launch)
        self.assertNotIn('new LikasForestView(this)',launch)
        self.assertIn('new Intent(this,MainActivity.class).putExtra("resume",true)',home)

    def test_pixel_forest_bounded_in_memory_and_frame_rate(self):
        forest = (JAVA / "LikasForestView.java").read_text(encoding="utf-8")
        self.assertIn('likas_forest/forest_day.webp', forest)
        self.assertIn('likas_forest/forest_night.webp', forest)
        self.assertIn("FRAME_INTERVAL_MS = 33L", forest)
        self.assertIn("postInvalidateDelayed(FRAME_INTERVAL_MS)", forest)
        self.assertIn("clockNightFraction()", forest)
        self.assertIn("imagePaint.setFilterBitmap(true)", forest)
        self.assertIn("forest_day_hd.webp", forest)
        self.assertIn("forest_night_hd.webp", forest)
        self.assertIn("ForestMotionPolicy.pan(seconds)", forest)
        self.assertIn("imageMatrix.postTranslate(x,", forest)
        self.assertIn("ValueAnimator.areAnimatorsEnabled()", forest)
        self.assertNotIn("WebView", forest)
        self.assertNotIn("requestLocationUpdates", forest)

    def test_survey_end_remains_available_to_observer(self):
        recording = (JAVA / "MainActivity.java").read_text(encoding="utf-8")
        self.assertIn('endSurveyButton=UiKit.button(this,"End Survey"', recording)
        self.assertIn('footerOptions.addView(endSurveyButton,endParams)', recording)
        self.assertIn('root.addView(footer)', recording)
        self.assertIn('footer.addView(saveButton,new LinearLayout.LayoutParams(-1,dp(', recording)
        self.assertIn('FieldLayoutPolicy.saveHeightDp(', recording)
        for item in ("Finish Complete", "End Incomplete", "Cancel Survey"):
            self.assertIn(item, recording)
        self.assertIn("exportZip()", recording)


if __name__ == "__main__":
    unittest.main()
