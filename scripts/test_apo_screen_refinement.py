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
        for item in ("filterPanel.addView(quick)", "filterPanel.addView(shortlistToggle,shortlistSpace)",
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
if __name__=="__main__": unittest.main()
