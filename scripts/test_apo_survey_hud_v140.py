"""RC80 compact HUD, station drafts, and outcome safety."""
import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def read(p):return (ROOT/p).read_text(encoding="utf-8")
class Rc80SurveyLayout(unittest.TestCase):
    def test_status_keeps_effort_and_gps_visible(self):
        s=read("app/src/main/java/org/phwildlife/survey/MainActivity.java")
        for phrase in ("compactClockView=label", "compactGpsView=label",
                       "statusDetailsPanel=hud", "hud.setVisibility(View.GONE)",
                       "syncCompactGps()", "Start GPS unverified",
                       "compactClockView.setText(summary)",
                       "compactGpsView.setOnClickListener(v->showGpsDetail())"):
            self.assertIn(phrase,s)
        self.assertIn("if(!largeText)root.addView(hud,hudLp)",s)
        self.assertIn("compactClockView.setVisibility(keyboardShown?View.GONE:View.VISIBLE)",s)
        self.assertIn('sessionClockView.setText("Session "+',s)
    def test_station_switch_checks_full_unsaved_draft(self):
        s=read("app/src/main/java/org/phwildlife/survey/MainActivity.java")
        for method in ("private void nextSamplingUnit(){","private void nextPoint(){"):
            part=s.split(method,1)[1].split("private ",1)[0]
            self.assertIn("if(hasUnsavedObservation())",part)
            self.assertIn("discardUnfinishedObservation()",part)
            self.assertIn("persistDraft()",part)
        self.assertIn("pendingPhoto!=null||markedAt>0||count!=1",s)
    def test_three_outcomes_unchanged(self):
        s=read("app/src/main/java/org/phwildlife/survey/MainActivity.java")
        self.assertIn('detailsToggle=label("Show method completion checks"',s)
        self.assertIn("SurveyClosePolicy.resolve(",s)
        for label in ('"Finish Complete"','"End Incomplete"','"Cancel Survey · Not Counted"'):
            self.assertIn(label,s)
        g=read("app/build.gradle")
        self.assertIn("versionCode 176",g)
        self.assertIn("versionName '0.27.4-rc116-localities-research-preview'",g)
        self.assertIn("org.phwildlife.survey.beta08",g)
        self.assertIn('super(context,NAME,null,29)',read("app/src/main/java/org/phwildlife/survey/FieldDb.java"))
if __name__=="__main__":unittest.main()
