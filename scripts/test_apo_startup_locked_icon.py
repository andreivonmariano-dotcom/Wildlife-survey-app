"""APO startup source contract; resource bytes are checked by Gradle preBuild.

The final locked art is the original forest_green_mountain_ribbon_icon.png.
No model-generated alternate logo or embedded text is permitted.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
MASTER_SHA = "6fe44e5117e0bde953b529735bc7d61a633f39201097a2fd68cd560e914ba0e7"
SPLASH_SHA = "a194803302b7e6c884068ff4e3e3926ba4a4e4110986e599e09a2e6dd7fd22bc"
FOREGROUND_SHA = "2a60cc5cda8dba006c1c713f15d565c4599ad5ec1bcaebadc7690cb66445af00"


class ApoStartupSourceContract(unittest.TestCase):
    def test_manifest_brand_and_identity(self):
        manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text()
        gradle = (ROOT / "app/build.gradle").read_text()
        self.assertIn('android:label="Apo"', manifest)
        self.assertIn('android:icon="@mipmap/apo_launcher"', manifest)
        self.assertIn('android:roundIcon="@mipmap/apo_launcher_round"', manifest)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'", gradle)
        self.assertIn("versionCode 132", gradle)
        self.assertIn(MASTER_SHA, gradle)
        self.assertIn(SPLASH_SHA, gradle)
        self.assertIn(FOREGROUND_SHA, gradle)
        self.assertIn('dependsOn("verifyApoArtwork")', gradle)

    def test_single_native_wordmark_and_noncircular_loading(self):
        launch = (ROOT / "app/src/main/java/org/phwildlife/survey/LikasLaunchActivity.java").read_text()
        self.assertIn('wordmark.setText("APO")', launch)
        self.assertEqual(launch.count('wordmark.setText("APO")'), 1)
        self.assertIn("R.drawable.apo_splash_mark", launch)
        self.assertIn("int markWidthDp", launch)
        self.assertIn("dp(markWidthDp*0.55f)", launch)
        self.assertIn("GradientDrawable.Orientation.TOP_BOTTOM", launch)
        self.assertIn("private static final int FOREST=Color.rgb(11,75,57)", launch)
        self.assertIn("private static final int DARK_FOREST=Color.rgb(2,25,24)", launch)
        self.assertIn("new int[]{FOREST,DARK_FOREST}", launch)
        self.assertIn("getWindow().setNavigationBarColor(DARK_FOREST)", launch)
        self.assertIn("root.setBackground(backdrop)", launch)
        # The startup background consists exclusively of the native two-stop
        # green gradient, with no landscape, tree, forest or scenery layer.
        self.assertEqual(launch.count("new GradientDrawable("), 1)
        self.assertNotIn("root.setBackgroundResource(", launch)
        self.assertNotIn("root.setBackgroundDrawable(", launch)
        self.assertNotIn("R.drawable.forest_", launch)
        self.assertNotIn("LikasForestView", launch)
        self.assertIn("Preparing offline references", launch)
        self.assertIn("BundledPrivateReferences.ensureInstalled", launch)
        self.assertIn('getBoolean("reduced_motion",false)', launch)
        self.assertIn("ValueAnimator.areAnimatorsEnabled()", launch)
        self.assertIn('home.putExtra("show_menu",true)', launch)
        self.assertNotIn("likas_natator_frog", launch)
        self.assertNotIn("LikasForestView", launch)

    def test_adaptive_resources_reference_approved_derivative(self):
        for name in ("apo_launcher.xml", "apo_launcher_round.xml"):
            xml = (ROOT / "app/src/main/res/mipmap-anydpi-v26" / name).read_text()
            self.assertIn("@drawable/apo_launcher_foreground", xml)
            self.assertIn("@color/apo_icon_background", xml)


if __name__ == "__main__":
    unittest.main()
