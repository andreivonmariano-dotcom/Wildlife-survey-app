"""RC51 scientific export query regression using synthetic SQLite only."""
from pathlib import Path
import re
import sqlite3
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name):
    return (JAVA/name).read_text()

class ResearchZipIntegrity(unittest.TestCase):
    def test_shared_eligibility_is_used_in_each_export(self):
        resolver=source("SurveyOutcomeSql.java")
        self.assertIn("public static final String COMPLETE",resolver)
        main=source("MainActivity.java")
        self.assertIn("static void writeResearchArchive(",main)
        self.assertIn("p.completed=1 AND (\"+SurveyOutcomeSql.RESOLVED+\")='complete'",main)
        self.assertNotIn("COALESCE(s.outcome,'active')",main)
        self.assertNotIn("COALESCE(outcome,'active')",main)
        for name in ("DistanceExport.java","ResearchExports.java"):
            self.assertIn("SurveyOutcomeSql.COMPLETE",source(name))
        self.assertNotIn("COALESCE(s.outcome,'complete')",source("DistanceExport.java"))
        self.assertNotIn("COALESCE(outcome,'complete')",source("DistanceExport.java"))

    def test_sqlite_fixture_recognizes_ended_legacy_without_promoting_active(self):
        expression="".join(re.findall(r'"([^"]*)"',
            source("SurveyOutcomeSql.java").split("RESOLVED =",1)[1].split(";",1)[0]))
        db=sqlite3.connect(":memory:")
        db.executescript("""
          CREATE TABLE surveys(id TEXT,outcome TEXT,ended TEXT,complete INTEGER);
          INSERT INTO surveys VALUES('modern','complete','2026-10-20',1);
          INSERT INTO surveys VALUES('legacy',NULL,'2026-10-20',1);
          INSERT INTO surveys VALUES('zero','complete','2026-10-20',1);
          INSERT INTO surveys VALUES('incomplete','incomplete','2026-10-20',0);
          INSERT INTO surveys VALUES('not_counted','not_counted','2026-10-20',1);
          INSERT INTO surveys VALUES('active',NULL,NULL,0);
          INSERT INTO surveys VALUES('anomaly',NULL,NULL,1);
        """)
        actual=[v[0] for v in db.execute(
            f"SELECT s.id FROM surveys s WHERE ({expression})='complete' ORDER BY s.id")]
        self.assertEqual(["legacy","modern","zero"],actual)

    def test_version_and_schema_stay_upgrade_compatible(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 124",gradle)
        self.assertIn("0.27.3-rc64-library-phone-icons-preview",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,27)",source("FieldDb.java"))
if __name__=="__main__":
    unittest.main()
