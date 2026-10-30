"""RC40 screenshot-driven home layout, copy, and legacy analysis contracts."""
import json
import re
import sqlite3
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

def source(name):
    return (JAVA/name).read_text(encoding="utf-8")

class Rc40ScreenAudit(unittest.TestCase):
    def test_three_home_destinations_are_content_sized(self):
        portal=source("LikasPortalUi.java")
        home=source("WelcomeActivity.java")
        body=portal.split("public static View tile(",1)[1].split(
            "/** Persistent field-session",1)[0]
        self.assertIn("LinearLayout panel=new LinearLayout(a)",body)
        self.assertIn("panel.setMinimumHeight(dp(a,108))",body)
        self.assertIn("panel.setOrientation(LinearLayout.HORIZONTAL)",body)
        self.assertNotIn("new FrameLayout(a);\n        panel.setBackground(",body)
        self.assertNotIn("panel.addView(backdrop",body)
        self.assertIn("new LinearLayout.LayoutParams(-1,-2)",home)
        main=home.split("private void home(){",1)[1].split("private void fieldworkHub(){",1)[0]
        for title in ('"Fieldwork"','"Library"','"Settings"'):
            self.assertIn(title,main)
        self.assertIn('this::fieldworkHub',main)
        self.assertIn('LikasLibraryActivity.class',main)
        self.assertIn('LikasSettingsActivity.class',main)

    def test_fieldwork_has_one_history_entry_and_clean_headings(self):
        home=source("WelcomeActivity.java")
        field=home.split("private void fieldworkHub(){",1)[1].split(
            "/** Short status caption",1)[0]
        self.assertNotIn("addHomeRecentSurvey()",field)
        self.assertEqual(field.count("HistoryActivity.class"),1)
        self.assertEqual(field.count('FieldVisuals.section(this,"New survey"'),1)
        self.assertNotIn('"Go to the field."',field)
        self.assertNotIn('step("NEW"',field)
        self.assertIn('recentSurveySummary()',field)
        for key in ("home","fieldwork","setup"):
            self.assertIn('scrollTop();',home)
        self.assertIn('shellScroll.post(()->shellScroll.scrollTo(0,0))',home)

    def test_setup_preserves_values_and_labels_visible_by_method(self):
        home=source("WelcomeActivity.java")
        for label in ('Point count duration (minutes)','Sampling duration (minutes)',
                      'Fixed radius (m)','Planned route length (km)','Station spacing'):
            self.assertIn('"'+label+'"',home)
        for name in ("durationLabel","timedLabel","radiusLabel"):
            self.assertIn(name+".setVisibility(",home)
        self.assertIn('durationEntry.setText("10")',home)
        self.assertIn('routeLengthEntry.setText("2")',home)
        self.assertIn('setupStartButton.setOnClickListener(v->startSelected())',home)

    def test_less_motion_and_galleries(self):
        main=source("MainActivity.java")
        self.assertIn('boolean reducedMotion=getSharedPreferences("likas_ui",MODE_PRIVATE)',main)
        self.assertIn('eventView.setAlpha(!reducedMotion&&!surveyPaused&&near?',main)
        self.assertIn('Button cancel=UiKit.button(this,"Cancel Survey · Not Counted","danger")',main)
        gallery=source("LikasDiscoveryActivity.java")
        self.assertIn('LayoutParams(dp(48),dp(48))',gallery)
        self.assertIn('TextView missing=note("Status unverified"',gallery)
        self.assertNotIn('"Priority species"',gallery)

    def test_shared_legacy_outcome_sql(self):
        policy=source("SurveyOutcomeSql.java")
        pieces=re.findall(r'"([^"]*)"',policy.split("RESOLVED =",1)[1])
        expression="".join(pieces)
        self.assertIn("s.outcome IS NOT NULL",expression)
        self.assertIn("s.ended IS NULL",expression)
        self.assertIn("COALESCE(s.complete,0)=1",expression)
        self.assertIn("SurveyOutcomeSql.RESOLVED",source("HistoryActivity.java"))
        dashboard=source("DashboardActivity.java")
        self.assertIn("String outcome=SurveyOutcomeSql.RESOLVED",dashboard)
        self.assertNotIn("s.outcome='complete'",dashboard)
        self.assertIn('section("Archived surveys"',dashboard)
        self.assertIn('if(active>0)section("Active surveys"',dashboard)

        db=sqlite3.connect(":memory:")
        db.executescript("""CREATE TABLE surveys(id TEXT, ended TEXT, complete INTEGER, outcome TEXT);
            INSERT INTO surveys VALUES
              ('legacy_complete','2025-01-01',1,NULL),
              ('legacy_incomplete','2025-01-01',0,NULL),
              ('modern_complete','2026-01-01',1,'complete'),
              ('modern_excluded','2026-01-01',0,'not_counted'),
              ('active',NULL,0,NULL);
        """)
        actual=dict(db.execute("SELECT "+expression+
            ",COUNT(*) FROM surveys s GROUP BY "+expression))
        self.assertEqual(actual,{"complete":2,"incomplete":1,"not_counted":1,"active":1})
        archived=db.execute("SELECT COUNT(*) FROM surveys s WHERE ("+
            expression+")<>'active'").fetchone()[0]
        self.assertEqual(archived,4)
        db.close()

    def test_direct_reference_copy_keeps_citations(self):
        habitat=source("HabitatSpeciesEvidenceActivity.java")
        self.assertNotIn("Published species–habitat context is not",habitat)
        self.assertIn('edge.optString("evidenceText")',habitat)
        self.assertIn('edge.optString("sourceTitle")',habitat)
        self.assertIn('putExtra("taxon_key",taxonKey)',habitat)
        card=source("Rc11PrioritySpeciesView.java")
        self.assertIn("ResearchCitationFormatter.citeClaim(",card)
        accounts=json.loads((ROOT/"app/src/main/assets/rc18_unified_96_priority_accounts_v1.json").read_text())
        self.assertEqual(len(accounts["accounts"]),96)
        facts=sum(sum(len(items) for items in entry.get("sections",{}).values())
                  for entry in accounts["accounts"])
        self.assertEqual(facts,456)
        texts=[item.get("text","") for entry in accounts["accounts"]
               for items in entry.get("sections",{}).values() for item in items]
        for bad in ("The study found","A Greater Mindanao study found",
                    "according to the museum taxonomy account",
                    "according to the American Society"):
            self.assertFalse(any(bad in text for text in texts),bad)

    def test_upgrade_keeps_signed_app_and_database_identity(self):
        build=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 107",build)
        self.assertIn("0.27.3-rc47-design-system-preview",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,27)",source("FieldDb.java"))

if __name__=="__main__":
    unittest.main()
