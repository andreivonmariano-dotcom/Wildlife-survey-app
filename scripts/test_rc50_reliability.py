"""RC50 reliability source/SQLite regression; app-owned data stays untouched."""
import re
import sqlite3
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
J = ROOT / "app/src/main/java/org/phwildlife/survey"

def contents(name):
    return (J/name).read_text(encoding="utf-8")

class Rc50Reliability(unittest.TestCase):
    def test_home_hero_not_measured_by_decorative_match_parent(self):
        source = contents("LikasPortalUi.java")
        hero = source.split("public static View hero(Activity a,boolean compact){",1)[1].split(
            "/** Compact content-sized navigation card.",1)[0]
        self.assertIn("cover.setWillNotDraw(false)",hero)
        self.assertIn("protected void onDraw(Canvas canvas)",hero)
        self.assertNotIn("cover.addView(contours",hero)

    def test_derived_exports_use_shared_legacy_outcome_resolution(self):
        source = contents("ResearchExports.java")
        self.assertIn("SurveyOutcomeSql.COMPLETE",source)
        self.assertNotIn("COALESCE(s.outcome,'active')='complete'",source)
        self.assertNotIn("COALESCE(outcome,'active')='complete'",source)
        self.assertIn('("+SurveyOutcomeSql.RESOLVED+"),e.habitat_code',source)

    def test_legacy_null_outcome_complete_without_fabricating_absence(self):
        source = contents("SurveyOutcomeSql.java")
        expr = source.split("RESOLVED =",1)[1].split(";",1)[0]
        parts = re.findall(r'"([^"]*)"',expr)
        resolved = "".join(parts)
        db = sqlite3.connect(":memory:")
        db.executescript("""
            CREATE TABLE surveys(id TEXT PRIMARY KEY, outcome TEXT, ended TEXT, complete INTEGER);
            CREATE TABLE point_counts(id TEXT, event_id TEXT);
            INSERT INTO surveys VALUES('legacy_complete',NULL,'2026-10-25',1);
            INSERT INTO surveys VALUES('legacy_incomplete',NULL,'2026-10-25',0);
            INSERT INTO surveys VALUES('unfinished',NULL,NULL,0);
            INSERT INTO surveys VALUES('cancelled','not_counted','2026-10-25',1);
            INSERT INTO surveys VALUES('complete','complete','2026-10-25',1);
            INSERT INTO point_counts SELECT id,id FROM surveys;
        """)
        actual=[r[0] for r in db.execute(
            f"SELECT p.id FROM point_counts p JOIN surveys s ON s.id=p.event_id "
            f"WHERE ({resolved})='complete' ORDER BY p.id")]
        self.assertEqual(["complete","legacy_complete"],actual)

    def test_restore_stages_all_attachments_before_database_activation(self):
        source=contents("FieldBackupArchive.java")
        self.assertLess(source.index("for(RestoreItem item:items){"),
                        source.index("moveNew(incomingDb,db)"))
        self.assertIn("restoreIntoEmptyForTest",source)
        self.assertIn("if(item.installed)",source)
        self.assertIn("if(item.originalMoved)",source)
        self.assertIn("checkDatabase(incomingDb)",source)
        self.assertIn("StandardCopyOption.ATOMIC_MOVE",source)

    def test_version_identity_and_schema_unchanged(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(gradle.split("versionCode ",1)[1].split()[0]),125)
        self.assertIn("0.27.3-rc65-library-optical-icons-preview",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,27)",contents("FieldDb.java"))

if __name__ == "__main__":
    unittest.main()
