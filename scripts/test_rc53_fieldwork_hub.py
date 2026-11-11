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
        self.assertIn("ApoHomeUi.module(this,index,title,detail,tap)",tile)
        self.assertNotIn("LikasPortalUi.action(",tile)
        self.assertNotIn("LikasPortalUi.mini(",tile)
        ui=source("ApoHomeUi.java")
        self.assertIn("row.setMinimumHeight(dp(a,compact?64:78))",ui)
        self.assertIn("new LinearLayout.LayoutParams(-1,-2)",ui)
        self.assertIn("R.drawable.apo_splash_mark",ui)
        section=text.split("private void fieldworkHub(){",1)[1].split("private void fieldRecordsHub(){",1)[0]
        for group in ("Birds","Herpetofauna","Mammals"):
            self.assertIn('surveyTile(main,"'+group+'"',section)
        self.assertIn('FieldVisuals.section(this,"New survey",null)',section)
    def test_current_survey_remains_first_and_interactive(self):
        section=source("WelcomeActivity.java").split("private void fieldworkHub(){",1)[1].split("private void fieldRecordsHub(){",1)[0]
        self.assertIn("String unfinished=activeSurvey()",section)
        self.assertIn("if(unfinished!=null)",section)
        self.assertIn('FieldVisuals.section(this,"Current survey",null)',section)
        self.assertIn('UiKit.button(this,"Resume Survey","primary")',section)
        self.assertIn('new Intent(this,MainActivity.class).putExtra("resume",true)',section)
        self.assertLess(section.index("Resume Survey"),section.index('FieldVisuals.section(this,"New survey"'))
        self.assertIn("}else{",section)
    def test_uniform_record_rows_and_reduced_tool_duplication(self):
        text=source("WelcomeActivity.java")
        workspace=text.split("private void myDataHub(){",1)[1].split("private void fieldworkHub(){",1)[0]
        self.assertEqual(workspace.count("ApoHomeUi.tool(this,"),6)
        for title in ('"Survey history"','"Projects & sites"','"Site habitat records"',
                      '"Analysis & exports"','"Field utilities"','"Backup & restore"'):
            self.assertIn(title,workspace)
        self.assertNotIn("LikasPortalUi.action(",workspace)
        self.assertNotIn("More field tools",workspace)
        self.assertIn("SitesActivity.class",workspace)
        for title in ("HistoryActivity.class","HabitatRecordsActivity.class",
                      "DashboardActivity.class","ResearchExportActivity.class",
                      "CallLibraryActivity.class","TrapProtocolActivity.class"):
            self.assertIn(title,text)
        self.assertNotIn('getBoolean("field_tools_open",false)',text)
    def test_version_data_and_artwork_contract_preserved(self):
        g=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(g.split("versionCode ",1)[1].split()[0]),125)
        self.assertIn("versionName \'0.27.4-",g)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",g)
        self.assertIn("super(context,NAME,null,29)",source("FieldDb.java"))
        ui=source("LikasPortalUi.java")
        for icon in ("likas_approved_birds_writhed_hornbill",
                     "likas_approved_herpetofauna_staurois_natator",
                     "likas_approved_mammals_rusa_marianna"):
            self.assertIn(icon,ui)
        recorder=source("MainActivity.java")
        self.assertIn('endSurveyButton.setOnClickListener(v->finishPrompt())',recorder)
if __name__=="__main__":unittest.main()
