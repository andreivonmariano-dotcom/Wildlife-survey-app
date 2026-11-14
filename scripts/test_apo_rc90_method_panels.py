"""RC90: survey method drives visible fields but not scientific record semantics."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"app/src/main/java/org/phwildlife/survey"
def read(name):return (SRC/name).read_text(encoding="utf-8")

class AdaptivePanels(unittest.TestCase):
    def test_17_methods_are_named_and_all_profiles_mapped(self):
        methods=read("SurveyMethods.java")
        policy=read("RecorderMethodUiPolicy.java")
        for method in ("point_count","line_transect","area_search","opportunistic",
            "visual_encounter","ves_transect","amphibian_calls","coverboard_search",
            "drift_fence_check","track_sign","spotlight","camera_trap_check",
            "bat_acoustic","live_trap_check"):
            self.assertIn('"'+method+'"',methods)
            self.assertIn('"'+method+'"',policy)
        self.assertIn("basic detection log only",policy)
        self.assertIn("Incidental",policy)
    def test_actual_main_view_bindings_are_method_specific(self):
        m=read("MainActivity.java")
        for part in (
          "RecorderMethodUiPolicy.focus(profile,method)",
          "RecorderMethodUiPolicy.firstDetectionProminent(profile,method)",
          "RecorderMethodUiPolicy.listeningProminent(profile,method)",
          "RecorderMethodUiPolicy.microhabitatProminent(profile,method)",
          "RecorderMethodUiPolicy.deviceIdProminent(profile,method)",
          "RecorderMethodUiPolicy.action(profile,method)",
          "case AUDIO:openCallLibrary();break",
          "case FIELD_EFFORT:openFieldProtocol();break",
          "case DEVICES:openTrapProtocol();break",
          "showFirst?View.VISIBLE:View.GONE",
          "listeningPriority?herpQuick:herpPanel",
          "listeningPriority?herpPanel:herpQuick",
          "microhabitatPriority)herpQuick.addView(microhabitatEntry)",
          "mammalQuick.addView(deviceEntry)",
          "else mammalPanel.addView(deviceEntry)"
        ):
            self.assertIn(part,m)
        self.assertIn('herpToggle.setOnClickListener',m)
        self.assertIn('mammalToggle.setOnClickListener',m)
        self.assertIn('birdToggle.setOnClickListener',m)
    def test_draft_schema_and_survey_completion_are_not_modified(self):
        m=read("MainActivity.java")
        self.assertIn("super(context,NAME,null,29)",read("FieldDb.java"))
        self.assertIn("versionCode 176",(ROOT/"app/build.gradle").read_text())
        self.assertIn("0.27.4-rc116-localities-research-preview",
            (ROOT/"app/build.gradle").read_text())
        for marker in ("SurveyClosePolicy.resolve(","Finish Complete",
            "End Incomplete","Cancel Survey · Not Counted",
            "if(hasUnsavedObservation())","INSERT INTO field_records(",
            "persistDraft(true)","refreshTimeline();"):
            self.assertIn(marker,m)
        policy=read("RecorderMethodUiPolicy.java")
        for dangerous in ("SQLiteDatabase","ContentValues","execSQL(","insert(",
            "UPDATE field_records","setSurveyOutcome"):
            self.assertNotIn(dangerous,policy)

if __name__=="__main__":unittest.main()
