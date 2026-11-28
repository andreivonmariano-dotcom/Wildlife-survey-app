"""Presentation-only refinement contracts; native geometry requires emulator QA."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(n): return (JAVA/(n+".java")).read_text(encoding="utf-8")
class ScreenRefinement(unittest.TestCase):
    def test_headers_and_nested_data(self):
        self.assertIn("heading.setAccessibilityHeading(true)",src("UiKit"))
        self.assertNotIn("heading.setEllipsize(",src("UiKit"))
        self.assertIn('if(i==4&&inData&&!"data".equals(screen))',src("ApoShellBar"))
    def test_picker_preserves_filters_and_result_space(self):
        s=src("SpeciesPickerActivity")
        for item in ("filterPanel.addView(quick)", "scrollBody.addView(shortlistToggle,1,shortlistSpace)",
                     "filterPanel.addView(controls)", "scrollBody.addView(cards)", "scrollBody.addView(status)",
                     'filterPanel.setVisibility(View.GONE)', "UiKit.fieldInput(this,search)",
                     "unknown.setOnClickListener", "refresh()"):
            self.assertIn(item,s)
    def test_scroll_long_citations(self):
        self.assertIn(".setView(detailScroll)",src("PhLibraryBibliographyActivity"))
        self.assertIn("detailScroll.addView(detail)",src("PhLibraryBibliographyActivity"))
    def test_camera_and_survey_invariants(self):
        self.assertIn("root.addView(bearingText,new LinearLayout.LayoutParams(-1,-2))",src("CameraActivity"))
        self.assertIn("capture.setOnClickListener(v->capture())",src("CameraActivity"))
        for label in ("Save Observation","End Survey","Cancel Survey · Not Counted"):
            self.assertIn(label,src("MainActivity"))
        self.assertIn("super(context,NAME,null,29)",src("FieldDb"))
    def test_field_preflight_and_reference_details(self):
        gps=src("SurveyGpsActivity")
        self.assertIn("Settings.ACTION_APPLICATION_DETAILS_SETTINGS",gps)
        self.assertNotIn("Tap Check current GPS",gps)
        self.assertIn("GpsFixPolicy.acceptableElapsed(",gps)
        recorder=src("MainActivity")
        self.assertIn("favoriteButton.setSelected(starred)",recorder)
        self.assertIn("countView.setMinWidth(dp(48))",recorder)
        self.assertIn("countLine.addView(countView,new LinearLayout.LayoutParams(-2,-2))",recorder)
        self.assertIn("UiKit.fieldInput(this,search)",src("TaxonDetailActivity"))
    def test_map_retains_scope_and_filters(self):
        s=src("ApoOfflineMapActivity")
        for key in ("map_mindanao","map_kind","map_project"):
            self.assertEqual(s.count('"'+key+'"'),2)
        self.assertIn("SQLiteDatabase.OPEN_READONLY",s)
        self.assertNotIn("getWritableDatabase",s)
if __name__=="__main__": unittest.main()
