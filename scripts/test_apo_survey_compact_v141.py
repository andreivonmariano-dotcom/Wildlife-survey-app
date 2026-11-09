"""Code141 tight-screen survey recorder regression contracts."""
import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def read(p):return (ROOT/p).read_text(encoding="utf-8")
class Rc81SurveyCompact(unittest.TestCase):
    def test_compact_clock_does_not_duplicate_opportunistic_time(self):
        s=read("app/src/main/java/org/phwildlife/survey/MainActivity.java")
        self.assertIn('("Session excl. pauses "',s)
        self.assertIn('if(hasStationUnits()||method.equals("line_transect")||method.equals("ves_transect"))',s)
        self.assertIn('summary+="\\n"+eventView.getText()',s)
        self.assertIn('Start GPS unverified',s)
        self.assertIn('compactGpsView.setOnClickListener(v->showGpsDetail())',s)
    def test_footer_320dp_can_be_inline_at_200pct(self):
        p=read("app/src/main/java/org/phwildlife/survey/FieldLayoutPolicy.java")
        self.assertIn('if(widthDp<320)return true;',p)
        self.assertIn('return scale>=2.4f&&widthDp<360;',p)
        s=read("app/src/main/java/org/phwildlife/survey/MainActivity.java")
        self.assertIn('fontScale>=1.9f?"Review":"Review Records"',s)
        self.assertIn('"End Survey","outline-danger"',s)
        self.assertIn('revealTools.setMinHeight(dp(56))',s)
    def test_two_distinct_emulator_capture_modes(self):
        t=read("app/src/androidTest/java/org/phwildlife/survey/RecorderUiEmulatorTest.java")
        self.assertIn('"apo_recorder_200pct_320dp.png":"apo_recorder_160pct.png"',t)
        self.assertIn('ultraLarge?"Review":"Review Records"',t)
        wf=read(".github/workflows/apo-survey-compact-v141-emulator-qa.yml")
        for fragment in ('font_scale 1.6','font_scale 2.0','wm size 960x2160',
                         'Identical screenshots from different configurations',
                         'versionCode=\'141\''):
            self.assertIn(fragment,wf)
    def test_version_and_scientific_record_identity(self):
        g=read("app/build.gradle")
        self.assertIn("versionCode 166",g)
        self.assertIn("0.27.3-rc106-phlibrary-polish",g)
        self.assertIn("org.phwildlife.survey.beta08",g)
        self.assertIn('super(context,NAME,null,29)',read("app/src/main/java/org/phwildlife/survey/FieldDb.java"))
        s=read("app/src/main/java/org/phwildlife/survey/MainActivity.java")
        self.assertIn("if(hasUnsavedObservation())",s)
        self.assertIn("SurveyClosePolicy.resolve(",s)
if __name__=="__main__":unittest.main()
