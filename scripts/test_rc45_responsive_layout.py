"""RC45 responsive layout source contracts; no survey or scientific data changes."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"

def source(name):
    return (JAVA / name).read_text(encoding="utf-8")

class Rc45LayoutContract(unittest.TestCase):
    def test_portals_share_width_and_text_scale_breakpoints(self):
        policy = source("FieldLayoutPolicy.java")
        self.assertIn("stackPortalPair(int availableWidthDp,float fontScale)", policy)
        self.assertIn("LikasDesignSystem.useSingleColumn(availableWidthDp,fontScale)", policy)
        for screen in ("WelcomeActivity.java", "LikasLibraryActivity.java",
                       "LikasSettingsActivity.java"):
            text = source(screen)
            self.assertIn("FieldLayoutPolicy.stackPortalPair(config.screenWidthDp,config.fontScale)",text)
            self.assertTrue("stackPortalPairs()?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL" in text
                            or "stacked?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL" in text)

    def test_picker_has_a_readable_search_field_and_unidentified_action(self):
        picker=source("SpeciesPickerActivity.java")
        self.assertIn("searchRow.addView(dropdown,new LinearLayout.LayoutParams(dp(48),dp(54)))",picker)
        self.assertIn("unidentifiedRow.addView(unknown,unknownSpace)",picker)
        self.assertIn("root.addView(unidentifiedRow)",picker)
        self.assertIn('Button unknown=UiKit.button(this,"Unidentified","secondary")',picker)
        self.assertLess(picker.index("root.addView(searchRow)"),
                        picker.index("root.addView(unidentifiedRow)"))
        self.assertNotIn("searchRow.addView(unknown,unknownSpace)",picker)

    def test_gallery_slots_are_shorter_and_lazy_hydration_is_preserved(self):
        gallery=source("LikasDiscoveryActivity.java")
        self.assertIn("int baseline=largeText()?(isHabitat?420:400):(isHabitat?330:310);",gallery)
        self.assertIn("slot.setMinimumHeight(dp(baseline));",gallery)
        self.assertNotIn("largeText()?625:410",gallery)
        self.assertIn("if(Math.abs(i-center)<=2)",gallery)
        self.assertIn("slot.removeAllViews()",gallery)
        self.assertIn("scroll.scrollTo(restoreOffset,0)",gallery)

    def test_header_wraps_at_large_text_and_touch_sizes_scale(self):
        ui=source("UiKit.java")
        self.assertIn("heading.setSingleLine(!largeText)",ui)
        self.assertIn("if(largeText)heading.setMaxLines(2)",ui)
        self.assertIn("back.setPadding(0,0,0,0)",ui)
        self.assertIn("row.setMinimumHeight(dp(a,largeText?64:52))",ui)
        recorder=source("MainActivity.java")
        self.assertIn("int touchDp=FieldLayoutPolicy.actionHeightDp(",recorder)
        self.assertIn("dp(touchDp)",recorder)
        self.assertIn('endSurveyButton=UiKit.button(this,"End Survey","outline-danger")',recorder)
        self.assertIn("root.addView(footer)",recorder)
        self.assertIn("finishPrompt()",recorder)

    def test_version_and_data_identity_stay_compatible(self):
        build=(ROOT/"app/build.gradle").read_text(encoding="utf-8")
        self.assertGreaterEqual(int(__import__("re").search(r"versionCode\s+(\d+)",build).group(1)),109)
        self.assertIn("0.27.3-rc",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,27)",source("FieldDb.java"))

if __name__=="__main__":
    unittest.main()
