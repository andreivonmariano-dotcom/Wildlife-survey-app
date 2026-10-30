"""RC53 screen 2 UI contracts. No app user records modified."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name):return (JAVA/name).read_text(encoding="utf8")

class Rc53FieldworkHub(unittest.TestCase):
    def test_one_column_survey_groups_across_screen_widths(self):
        text=source("WelcomeActivity.java")
        tile=text.split("private void surveyTile(",1)[1].split("private void home(){",1)[0]
        self.assertIn("LikasPortalUi.action(this,title,detail,art,tap)",tile)
        self.assertIn("new LinearLayout.LayoutParams(-1,-2)",tile)
        self.assertNotIn("LikasPortalUi.mini(",tile)
        section=text.split("private void fieldworkHub(){",1)[1].split("/** Short status caption",1)[0]
        for group in ("Birds","Herpetofauna","Mammals"):
            self.assertIn('surveyTile(main,"'+group+'"',section)
        self.assertNotIn("new LinearLayout(this)",section)
        self.assertIn('FieldVisuals.section(this,"New survey","Choose a wildlife group")',section)

    def test_current_survey_remains_first_and_interactive(self):
        section=source("WelcomeActivity.java").split("private void fieldworkHub(){",1)[1].split("/** Short status caption",1)[0]
        self.assertIn("String unfinished=activeSurvey()",section)
        self.assertIn("if(unfinished!=null)",section)
        self.assertIn('FieldVisuals.section(this,"Current survey",null)',section)
        self.assertIn('UiKit.button(this,"Resume Survey","primary")',section)
        self.assertIn('new Intent(this,MainActivity.class).putExtra("resume",true)',section)
        self.assertLess(section.index("Resume Survey"),section.index('FieldVisuals.section(this,"Records & projects"'))
        self.assertIn("}else{",section)

    def test_uniform_record_rows_and_reduced_tool_duplication(self):
        section=source("WelcomeActivity.java").split("private void fieldworkHub(){",1)[1].split("/** Short status caption",1)[0]
        for title in ('"Projects & stations"','"History"','"Analysis"'):
            self.assertIn(title,section)
        self.assertEqual(section.count("LikasPortalUi.action(this,"),2)
        self.assertNotIn("LikasPortalUi.mini(",section)
        self.assertNotIn('FieldVisuals.section(this,"Field tools"',section)
        self.assertIn('"More field tools"',section)
        self.assertIn('"Hide field tools"',section)
        self.assertIn('getBoolean("field_tools_open",false)',section)
        self.assertIn("addFieldworkRow(main,history)",section)
        self.assertIn("addFieldworkRow(main,dashboard)",section)
        self.assertIn("addFieldworkRow(parent,item)",source("WelcomeActivity.java"))

    def test_version_data_and_artwork_contract_preserved(self):
        g=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 116",g)
        self.assertIn("0.27.3-rc56-live-recorder-preview",g)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",g)
        self.assertIn("super(context,NAME,null,27)",source("FieldDb.java"))
        ui=source("LikasPortalUi.java")
        for icon in ("likas_approved_birds_writhed_hornbill",
                     "likas_approved_herpetofauna_staurois_natator",
                     "likas_approved_mammals_rusa_marianna"):
            self.assertIn(icon,ui)
        recorder=source("MainActivity.java")
        self.assertIn('endSurveyButton.setOnClickListener(v->finishPrompt())',recorder)
if __name__=="__main__":unittest.main()
