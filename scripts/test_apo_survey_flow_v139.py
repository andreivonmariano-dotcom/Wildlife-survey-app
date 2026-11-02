"""RC79 recorder-first phone layout, completion warnings and immutable science gates."""
import re
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
def read(path):
    return (ROOT/path).read_text(encoding="utf-8")

class RecorderFlowRC79(unittest.TestCase):
    def test_large_type_observation_first_without_hiding_station_methods(self):
        s=read("app/src/main/java/org/phwildlife/survey/MainActivity.java")
        self.assertIn('compactObservationFirst=!hasStationUnits()',s)
        self.assertIn('!method.equals("line_transect")&&!method.equals("ves_transect")',s)
        self.assertIn('recorderTools.setVisibility(View.GONE)',s)
        self.assertIn('revealTools.setOnClickListener',s)
        self.assertLess(s.index('content.addView(revealTools'),s.index('FieldVisuals.section(this,"Observation"'))
        self.assertIn('pointNextButton.setVisibility(hasStationUnits()?View.VISIBLE:View.GONE)',s)

    def test_two_column_footer_still_has_persistent_end(self):
        s=read("app/src/main/java/org/phwildlife/survey/MainActivity.java")
        p=read("app/src/main/java/org/phwildlife/survey/FieldLayoutPolicy.java")
        self.assertIn('boolean stackDock=FieldLayoutPolicy.stackRecorderDock(',s)
        self.assertIn('endSurveyButton.setOnClickListener(v->finishPrompt())',s)
        self.assertIn('root.addView(footer)',s)
        self.assertIn('footerOptions.addView(endSurveyButton,endParams)',s)
        self.assertIn('public static boolean stackRecorderDock(',p)
        self.assertIn('return scale>=2.4f&&widthDp<360;',p)
        self.assertIn('Math.min(80,Math.round(50f*scale))',p)

    def test_discard_confirmation_covers_unsaved_method_details(self):
        s=read("app/src/main/java/org/phwildlife/survey/MainActivity.java")
        closure=s.split("private boolean hasUnsavedObservation(){",1)[1].split("private void finishPrompt()",1)[0]
        for name in ("lifeStageSpinner","distanceSourceSpinner","firstPositionSpinner","movementSpinner"):
            self.assertIn(name,closure)
            self.assertIn("if("+name+"!=null)"+name+".setSelection(0)",closure)
        self.assertIn('label(completionChecklist(),12,false)',s)
        self.assertIn('SurveyClosePolicy.resolve(',s)
        self.assertIn('"Cancel Survey · Not Counted"',s)

    def test_emulator_and_release_identity(self):
        test=read("app/src/androidTest/java/org/phwildlife/survey/RecorderUiEmulatorTest.java")
        for fragment in ("Nonessential survey tools must begin collapsed",
                         "Sampling tools should expand on demand",
                         "End Survey should fit within viewport"):
            self.assertIn(fragment,test)
        gradle=read("app/build.gradle")
        self.assertIn("versionCode 145",gradle)
        self.assertIn("versionName '0.27.3-rc85-apo-recorder-species'",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn('super(context,NAME,null,28)',read("app/src/main/java/org/phwildlife/survey/FieldDb.java"))

if __name__=="__main__":
    unittest.main()
