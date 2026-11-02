"""RC75 BioTools destination UX and data-safety contracts."""
import re
import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(name):return (JAVA/name).read_text(encoding="utf-8")

class ApoBioToolsDestinations(unittest.TestCase):
    def test_nested_tools_are_text_only_and_reflow(self):
        ui=src("ApoHomeUi.java")
        self.assertIn("public static View tool(",ui)
        self.assertIn("line.setMinimumHeight(dp(a,72))",ui)
        self.assertIn("setContentDescription",ui)
        self.assertIn("new LinearLayout.LayoutParams(0,-2,1f)",ui)
        self.assertNotIn("R.drawable.",ui)
        home=src("WelcomeActivity.java")
        for name in ("fieldRecordsHub","analysisHub","fieldUtilitiesHub"):
            section=home.split("private void "+name+"(){",1)[1].split("UiMotion.enter(this,main)",1)[0]
            self.assertIn("ApoHomeUi.tool(",section)
            self.assertNotIn("LikasPortalUi.action(",section)
        self.assertIn("savedRecordCounts()",home)
        self.assertIn("WHERE deleted_at IS NULL",home)

    def test_stations_have_search_and_validate_coordinates(self):
        s=src("SitesActivity.java")
        for token in ("stationQuery","setContentDescription",
                      "Search station name, ID or habitat",
                      "new android.text.TextWatcher()",
                      "renderSites()","ApoHomeUi.tool(",
                      "Coordinates not recorded","c.isNull(3)||c.isNull(4)",
                      "Double.isFinite(value)",'setError("Invalid latitude")',
                      'setError("Invalid longitude")'):
            self.assertIn(token,s)
        self.assertIn('values.putNull("coordinate_uncertainty_m")',s)
        self.assertIn('values.put("updated_at",now())',s)
        self.assertIn('String random=UUID.randomUUID()',s)
        self.assertNotIn("DROP TABLE",s)

    def test_history_outcomes_are_read_only_and_explicit(self):
        s=src("HistoryActivity.java")
        self.assertIn('eventFilter==null?"Field Records":"All surveys"',s)
        self.assertIn('"SELECT "+OUTCOME_SQL+",COUNT(*) FROM surveys s GROUP BY "+OUTCOME_SQL',s)
        self.assertIn('"not_counted".equals(state)',s)
        self.assertIn('c.isNull(4)?"Date not recorded"',s)
        self.assertIn('"No matching surveys. Adjust the status or search text."',s)

    def test_dashboard_distinguishes_effort_from_biology(self):
        s=src("DashboardActivity.java")
        for text in ("String outcome=SurveyOutcomeSql.RESOLVED",
                     '"Archived surveys"','"Bird point-count visits"',
                     '"Other sampling-unit records"',
                     '"Detections in complete surveys"',
                     "not verified taxa","not a density estimate",
                     "Zero-detection effort can still be valid",
                     "not unique visits","habitat selection"):
            self.assertIn(text,s)
        self.assertNotIn("String.valueOf(visits+otherVisits)",s)
        self.assertNotIn("PRAGMA quick_check",s)

    def test_audit_owns_database_integrity(self):
        s=src("AuditActivity.java")
        self.assertIn("PRAGMA quick_check",s)
        self.assertIn('new FieldDb(getApplicationContext())',s)
        self.assertIn('"Data quality review","Analysis & Exports"',s)
        self.assertIn("not necessarily unique observations",s)
        self.assertIn("auditWorker.execute(()->",s)

    def test_field_utilities_preserve_active_survey_rules(self):
        audio=src("CallLibraryActivity.java")
        trap=src("TrapProtocolActivity.java")
        fields=src("FieldProtocolsActivity.java")
        study=src("StudyProtocolsActivity.java")
        self.assertIn("if(canRecord())",audio)
        self.assertIn("To record new survey audio",audio)
        self.assertIn("if(writable())",trap)
        self.assertIn('"Trap & camera records"',trap)
        self.assertIn('FieldProtocolPolicy.supported(method)',fields)
        self.assertIn('eventId==null?"Field Utilities"',fields)
        self.assertIn('UiKit.fieldInput(this,e)',study)
        self.assertIn("Already saved surveys keep",study)
        self.assertIn("StudyProtocols",src("WelcomeActivity.java"))

    def test_locked_identity_and_export_writer_remain(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(re.search(r"versionCode\\s+(\\d+)",gradle).group(1)),135)
        self.assertIn("0.27.3-rc",gradle)
        self.assertIn("org.phwildlife.survey.beta08",gradle)
        self.assertIn("super(context,NAME,null,28)",src("FieldDb.java"))
        self.assertIn("MainActivity.writeResearchArchive(",src("ResearchExportActivity.java"))
        main=src("MainActivity.java")
        self.assertIn('endSurveyButton.setOnClickListener(v->finishPrompt())',main)
        self.assertIn('static void writeResearchArchive(',main)

if __name__=="__main__":unittest.main()
