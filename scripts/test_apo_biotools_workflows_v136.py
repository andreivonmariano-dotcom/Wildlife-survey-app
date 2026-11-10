"""RC76 design and workflow source contracts for nested BioTools screens."""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(file):return (JAVA/file).read_text(encoding="utf-8")

class ApoBioToolsWorkflowsV136(unittest.TestCase):
    def test_history_filter_scroll_and_back_to_list(self):
        s=src("HistoryActivity.java")
        self.assertIn("ScrollView scroll=new ScrollView(this);",s)
        self.assertIn("LinearLayout scrollBody=column();",s)
        self.assertIn("scrollBody.addView(filters);",s)
        self.assertIn("scrollBody.addView(rows,new LinearLayout.LayoutParams(-1,-2))",s)
        self.assertIn("filters.addView(outcomes",s)
        self.assertIn("filters.addView(searchRow,searchLp)",s)
        self.assertNotIn("root.addView(searchRow,searchLp)",s)
        self.assertIn("@Override public void onBackPressed()",s)
        self.assertIn("eventFilter=null;",s)
        self.assertIn("surveyOffset=0;",s)
        self.assertIn("searchHandler.postDelayed(pendingSearch,220L)",s)
        self.assertIn("SurveyOutcomeSql.RESOLVED",s)

    def test_four_stage_survey_setup_unchanged(self):
        s=src("WelcomeActivity.java")
        for item in ('step("01","Survey method")','step("02","Project and location")',
                     'step("03","Sampling effort")','step("04","Begin survey")',
                     '"Continue to GPS"','methodChoices.check(1000)',
                     'setupFooter.setVisibility(View.VISIBLE)',
                     'setupStartButton.setOnClickListener(v->startSelected())',
                     '"Standard survey requirements"'):
            self.assertIn(item,s)
        self.assertIn('label.setContentDescription("Step "+number+": "+title)',s)
        self.assertIn('ApoHomeUi.note(this,',s)
        self.assertIn('startActivity(new Intent(this,SitesActivity.class))',s)
        self.assertIn('refreshRouteTemplates()',s)
        self.assertIn('StudyProtocols',s)

    def test_study_templates_have_readable_inputs_and_edit_confirmations(self):
        s=src("StudyProtocolsActivity.java")
        for item in ('"Survey context"','"Completion thresholds"',
                     '"Evidence requirements"','"Saved templates"',
                     '"Minimum completed visits"','"Minimum active sampling minutes"',
                     'UiKit.fieldInput(this,e)',
                     'ApoHomeUi.tool(this,',
                     '"Edit template","Delete template"',
                     '.setTitle("Delete this template?")',
                     'editTemplate(id,methodCode,name,oldVisits,',
                     'store.getWritableDatabase().delete('):
            self.assertIn(item,s)
        self.assertIn('study_protocol_templates',s)
        self.assertIn('survey_profile',s)

    def test_export_preflight_is_informative_and_read_only(self):
        s=src("ResearchExportActivity.java")
        for item in ('recordSummary()','"SELECT COUNT(*) FROM surveys"',
                     'WHERE deleted_at IS NULL','"SELECT COUNT(*) FROM projects"',
                     'Surveys of all outcomes','sensitive species locations',
                     'View.ACCESSIBILITY_LIVE_REGION_POLITE',
                     'Intent.ACTION_CREATE_DOCUMENT','ZipOutputStream',
                     'MainActivity.writeResearchArchive(getApplicationContext(),db,zip)',
                     'Export cancelled'):
            self.assertIn(item,s)
        self.assertNotIn('getWritableDatabase()',s)
        self.assertNotIn('deleteDatabase(',s)

    def test_audit_findings_are_scrolled_not_pinned(self):
        s=src("AuditActivity.java")
        self.assertIn('scroll.setFillViewport(true)',s)
        self.assertIn('body.addView(refreshButton)',s)
        self.assertIn('body.addView(integrity,integrityLp)',s)
        self.assertIn('body.addView(items,new LinearLayout.LayoutParams(-1,-2))',s)
        self.assertIn('new FieldDb(getApplicationContext())',s)
        self.assertIn('DataAudit.run(',s)
        self.assertIn('PRAGMA quick_check',s)
        self.assertIn('auditWorker.execute(()->',s)

    def test_no_data_or_signing_changes(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(re.search(r"versionCode\s+(\d+)",gradle).group(1)),136)
        self.assertIn('0.27.3-rc',gradle)
        self.assertIn("org.phwildlife.survey.beta08",gradle)
        self.assertIn('super(context,NAME,null,29)',src("FieldDb.java"))
        main=src("MainActivity.java")
        self.assertIn('endSurveyButton.setOnClickListener(v->finishPrompt())',main)
        self.assertIn('static void writeResearchArchive(',main)

if __name__=="__main__":unittest.main()
