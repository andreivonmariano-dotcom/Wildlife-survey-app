"""APO RC77 end-to-end source contracts across all five BioTools destinations."""
import re
import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(n):return (JAVA/n).read_text(encoding="utf-8")

class AllBioToolsScreens(unittest.TestCase):
    def test_hub_and_all_destinations_are_explicit(self):
        s=src("WelcomeActivity.java")
        hub=s.split("private void bioToolsHub(){",1)[1].split("private void fieldworkHub(){",1)[0]
        self.assertEqual(hub.count("ApoHomeUi.module(this,"),5)
        for name in ("Surveys","Projects & Stations","Field Records",
                     "Analysis & Exports","Field Utilities"):
            self.assertIn('"'+name+'"',hub)
        for cls in ("SitesActivity","HistoryActivity","HabitatRecordsActivity",
                    "DashboardActivity","ResearchExportActivity","AuditActivity",
                    "CallLibraryActivity","TrapProtocolActivity",
                    "FieldProtocolsActivity","StudyProtocolsActivity"):
            self.assertIn(cls+".class",s)
        self.assertIn('putExtra("initial_tab","records")',s)
        self.assertIn('SurveyMethods.lookup(',s)
        self.assertIn('putExtra("resume",true)',s)

    def test_habitat_records_open_correct_context_and_preserve_unknowns(self):
        s=src("HabitatRecordsActivity.java")
        self.assertIn('selectedTab="records".equals(getIntent().getStringExtra("initial_tab"))?1:0;',s)
        self.assertIn('selectedTab==1?"Field Records":"PHLibrary"',s)
        self.assertIn('researchToggle.setVisibility(selectedTab==0?',s)
        self.assertIn("recordSummary()",s)
        self.assertIn('FROM station_environment',s)
        self.assertIn("vertical_uncertainty_m",s)
        self.assertIn("Accuracy not recorded",s)
        self.assertIn("Habitat not recorded",s)
        self.assertIn("guideTab.setContentDescription(",s)
        self.assertIn("recordsTab.setContentDescription(",s)
        self.assertNotIn("getWritableDatabase()",s)

    def test_audio_browse_and_review_have_scalable_actions(self):
        s=src("CallLibraryActivity.java")
        self.assertIn('ApoHomeUi.section(this,"Acoustic evidence"',s)
        self.assertIn('new LinearLayout.LayoutParams(-1,-2)',s)
        self.assertIn('FieldLayoutPolicy.stackPortalPair(',s)
        self.assertIn('if(selectedClip!=null){',s)
        self.assertIn('@Override public void onBackPressed()',s)
        self.assertIn("canRecord()",s)
        self.assertIn("ApoHomeUi.note(this",s)
        self.assertIn("CallSpectrogramView",s)

    def test_camera_and_trap_surveys_keep_all_safety_gates(self):
        s=src("TrapProtocolActivity.java")
        self.assertIn("ApoHomeUi.tool(",s)
        self.assertIn("scrollForm(form)",s)
        self.assertIn("UiKit.fieldInput(this,x)",s)
        self.assertIn("@Override public void onBackPressed()",s)
        self.assertIn("TrapProtocolPolicy.supported(method)",s)
        self.assertIn("if(writable())",s)
        self.assertIn("new AlertDialog.Builder(this)",s)
        self.assertIn('"No recorded device surveys yet.',s)
        self.assertIn('getWritableDatabase().update("trap_deployments"',s)

    def test_field_methods_preserve_effort_and_evidence_rules(self):
        s=src("FieldProtocolsActivity.java")
        self.assertIn('ApoHomeUi.tool(this,',s)
        self.assertIn('ApoHomeUi.section(this,"Recorded field method"',s)
        self.assertIn("UiKit.fieldInput(this,e)",s)
        self.assertIn("scrollForm(form)",s)
        self.assertIn("@Override public void onBackPressed()",s)
        self.assertIn("if(!active())",s)
        self.assertIn('FieldProtocolPolicy.supported(method)',s)
        self.assertIn("stampGps(ContentValues",s)
        self.assertIn("Zero-detection inspections remain valid",s)

    def test_small_screen_dialogs_and_existing_archived_editing(self):
        self.assertIn("projectForm.addView(form);",src("SitesActivity.java"))
        self.assertIn('stationQuery.toLowerCase(Locale.ROOT)',src("SitesActivity.java"))
        self.assertIn("scrollForm(panel)",src("StudyProtocolsActivity.java"))
        self.assertIn('store.getWritableDatabase().update("study_protocol_templates"',src("StudyProtocolsActivity.java"))
        self.assertIn("scrollForm(form)",src("CallLibraryActivity.java"))
        self.assertIn("scrollBody.addView(filters);",src("HistoryActivity.java"))
        self.assertIn("surveyOffset+=PAGE_SIZE",src("HistoryActivity.java"))
        self.assertIn("SurveyOutcomeSql.RESOLVED",src("DashboardActivity.java"))
        self.assertIn("MainActivity.writeResearchArchive(",src("ResearchExportActivity.java"))
        self.assertIn("PRAGMA quick_check",src("AuditActivity.java"))

    def test_surveys_brand_and_schema_remain_unchanged(self):
        b=(ROOT/"app/build.gradle").read_text(encoding="utf-8")
        self.assertGreaterEqual(int(re.search(r"versionCode\s+(\d+)",b).group(1)),137)
        self.assertIn("0.27.3-rc",b)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",b)
        self.assertIn("super(context,NAME,null,28)",src("FieldDb.java"))
        main=src("MainActivity.java")
        for x in ('"Save Observation"','"End Survey"','"Cancel Survey · Not Counted"',
                  'SurveyClosePolicy.resolve('):
            self.assertIn(x,main)

if __name__=="__main__":unittest.main()
