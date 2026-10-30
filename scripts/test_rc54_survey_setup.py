"""RC54: native input semantics and screen 3 visual contracts only."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def read(name):return (JAVA/name).read_text(encoding="utf8")
class Rc54SetupVisualContract(unittest.TestCase):
    def test_setup_has_single_gps_action_and_all_four_steps(self):
        a=read("WelcomeActivity.java")
        setup=a.split("private void setup(String selected){",1)[1].split(
            "private void refreshStudyTitle()",1)[0]
        for number,heading in [("01","Survey method"),("02","Project and location"),
                               ("03","Sampling effort"),("04","Begin survey")]:
            self.assertIn('step("'+number+'","'+heading+'")',setup)
        self.assertIn('setupFooter.setVisibility(View.VISIBLE)',setup)
        self.assertIn('setupStartButton.setOnClickListener(v->startSelected())',a)
        self.assertNotIn('UiKit.button(this,"Continue to GPS"',setup)

    def test_labels_and_project_reflow_use_shared_rules(self):
        a=read("WelcomeActivity.java")
        self.assertIn('private TextView setupLabel(String value)',a)
        self.assertIn('private void setupSpinner(Spinner spinner,String description)',a)
        self.assertIn('boolean stackProjectControls=stackPortalPairs()',a)
        self.assertIn('setupSpinner(geographicChoice,"Choose checklist region")',a)
        for x in ('Project','Checklist region','Point count duration (minutes)',
                  'Sampling duration (minutes)','Fixed radius (m)',
                  'Station spacing','Planned route length (km)',
                  'Transect magnetic bearing (0–359°)'):
            self.assertIn('setupLabel("'+x+'")',a)
        self.assertIn("bearingLabel.setVisibility(showBearing?View.VISIBLE:View.GONE)",a)
        self.assertIn("lineBearingEntry.setVisibility(showBearing?View.VISIBLE:View.GONE)",a)
        self.assertIn("setupSpinner(studyChoice,",a)

    def test_editable_inputs_and_disclosures_remain_native(self):
        a=read("WelcomeActivity.java")
        for token in (
          "UiKit.fieldInput(this,input)",
          "methodChoices=new RadioGroup(this)",
          "rb.setButtonTintList(ColorStateList.valueOf(UiKit.POND))",
          "methodChoices.setOnCheckedChangeListener((group,checked)->",
          "styleSetupInput(entry)",
          'advancedToggle.setContentDescription("Show additional survey details")',
          'studyToggle.setContentDescription("Show optional study rules")',
          'new Intent(this,SurveyGpsActivity.class)',
          'persistSetup();',
          'radiusEntry.setError("Enter 1 to 2000 meters or leave blank")',
        ):self.assertIn(token,a)
        self.assertIn("versionCode 116",(ROOT/"app/build.gradle").read_text())
        self.assertIn("0.27.3-rc56-live-recorder-preview",(ROOT/"app/build.gradle").read_text())
        self.assertIn("super(context,NAME,null,27)",read("FieldDb.java"))
if __name__=="__main__":unittest.main()
