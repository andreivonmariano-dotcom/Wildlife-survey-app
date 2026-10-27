"""Exercise real additive FieldDb SQL migrations 23->27 and SQLite backup/restore.

Uses source SQL extracted from the Java migration methods, not duplicate SQL
invented for these tests. Android device upgrades still require handset QA.
"""
from pathlib import Path
import json
import re
import sqlite3
import tempfile
import unittest

DB_SOURCE = (Path(__file__).resolve().parents[1] /
             "app/src/main/java/org/phwildlife/survey/FieldDb.java").read_text(encoding="utf-8")
VERSIONS = {24:"TwentyFour",25:"TwentyFive",26:"TwentySix",27:"TwentySeven"}

def migration_sql(version):
    marker = "private void upgrade" + VERSIONS[version] + "(SQLiteDatabase d){"
    start = DB_SOURCE.index(marker) + len(marker)
    end = DB_SOURCE.index("\n    }", start)
    body = DB_SOURCE[start:end]
    statements = []
    for statement in re.findall(r"d\.execSQL\((.*?)\);", body, flags=re.S):
        # No SQL variables are allowed in this pinned run of additive migrations.
        fragments = re.findall(r'"(?:\\.|[^"\\])*"', statement)
        remainder = re.sub(r'"(?:\\.|[^"\\])*"|[+\s]', "", statement)
        if remainder or not fragments:
            raise AssertionError("Migration SQL contains unrecognized expressions: " + remainder)
        sql = "".join(json.loads(piece) for piece in fragments)
        statements.append(sql)
    if len(statements) < 2:
        raise AssertionError("Upgrade extracted no SQL")
    return statements

def create_v23():
    db = sqlite3.connect(":memory:")
    db.execute("PRAGMA foreign_keys=ON")
    db.executescript("""
    CREATE TABLE surveys(id TEXT PRIMARY KEY,method TEXT,complete INTEGER,ended TEXT);
    CREATE TABLE field_records(id INTEGER PRIMARY KEY,event_id TEXT,raw_identification TEXT);
    CREATE TABLE acoustic_labels(id INTEGER PRIMARY KEY,clip_id TEXT,start_ms INTEGER,
        end_ms INTEGER,call_label TEXT,notes TEXT,reviewed_at_utc TEXT);
    INSERT INTO surveys VALUES('sv-old','amphibian_calls',1,'2026-10-22T12:00:00Z');
    INSERT INTO field_records VALUES(42,'sv-old','Unknown amphibian');
    INSERT INTO acoustic_labels VALUES(7,'clip-old',0,400,'Manual frog',NULL,'2026-10-22T11:00:00Z');
    PRAGMA user_version=23;
    """)
    return db

def migrate(db, start=24, finish=27):
    for version in range(start, finish+1):
        with db:
            for query in migration_sql(version):
                db.execute(query)
            db.execute("PRAGMA user_version="+str(version))

class RealSchemaMigrationTests(unittest.TestCase):
    def test_older_observations_and_acoustics_survive(self):
        db=create_v23()
        migrate(db)
        self.assertEqual(27,db.execute("PRAGMA user_version").fetchone()[0])
        self.assertEqual("Unknown amphibian",
            db.execute("SELECT raw_identification FROM field_records WHERE id=42").fetchone()[0])
        self.assertEqual(("Manual frog", "unverified"),
            db.execute("SELECT call_label,review_status FROM acoustic_labels WHERE id=7").fetchone())
        self.assertEqual("ok",db.execute("PRAGMA integrity_check").fetchone()[0])
        db.close()

    def test_existing_devices_board_records_and_study_templates_survive(self):
        db=create_v23()
        migrate(db,24,24)
        db.execute("""INSERT INTO trap_deployments
            (id,event_id,method,device_id,deployed_at_utc)
            VALUES('dev1','sv-old','camera_trap_check','C-001','2026-10-22T10:00:00Z')""")
        migrate(db,25,25)
        db.execute("""INSERT INTO coverboard_units
            (id,event_id,board_id,installed_at_utc)
            VALUES('board1','sv-old','B001','2026-09-22T00:00:00Z')""")
        db.execute("""INSERT INTO coverboard_checks
            (id,board_pk,checked_at_utc,observer,board_condition,individuals_seen)
            VALUES(3,'board1','2026-10-22T11:30:00Z','Observer','Intact and checked',0)""")
        migrate(db,26,26)
        db.execute("""INSERT INTO study_protocol_templates
            (id,project_id,method,name,min_visits,min_minutes,min_units,
             require_verified_gps,require_protocol_photo,created_at_utc)
            VALUES('tpl-legacy','project-old','spotlight','Legacy study',1,10,1,0,0,
             '2026-10-22T12:00:00Z')""")
        db.execute("""INSERT INTO survey_study_protocols
            (event_id,template_id,template_name,min_visits,min_minutes,
             min_units,require_verified_gps,require_protocol_photo,applied_at_utc)
            VALUES('sv-old','tpl-legacy','Legacy study',1,10,1,0,0,
            '2026-10-22T12:00:00Z')""")
        migrate(db,27,27)
        self.assertEqual(("tpl-legacy",None),db.execute(
            "SELECT id,survey_profile FROM study_protocol_templates").fetchone())
        self.assertEqual(("Legacy study",1,10),db.execute(
            "SELECT template_name,min_visits,min_minutes FROM survey_study_protocols").fetchone())
        self.assertEqual((0,"missing"),db.execute(
            "SELECT individuals_seen,gps_status FROM coverboard_checks WHERE id=3").fetchone())
        self.assertEqual("C-001",db.execute(
            "SELECT device_id FROM trap_deployments WHERE id='dev1'").fetchone()[0])
        self.assertEqual("ok",db.execute("PRAGMA integrity_check").fetchone()[0])
        self.assertEqual([],db.execute("PRAGMA foreign_key_check").fetchall())
        db.close()

    def test_backup_restore_preserves_ids_and_evidence(self):
        source=create_v23()
        migrate(source)
        source.execute("""INSERT INTO study_protocol_templates
            (id,project_id,method,survey_profile,name,min_visits,min_minutes,min_units,
             require_verified_gps,require_protocol_photo,created_at_utc)
            VALUES('frog-rule','field-1','amphibian_calls','Herpetofauna','Night calls',
             2,30,1,0,1,'2026-10-27T10:00:00Z')""")
        source.commit()
        with tempfile.TemporaryDirectory() as tmp:
            backup=Path(tmp)/"likas-backup.sqlite"
            restored=Path(tmp)/"likas-restored.sqlite"
            with sqlite3.connect(backup) as dest: source.backup(dest)
            with sqlite3.connect(backup) as prior,sqlite3.connect(restored) as target:
                prior.backup(target)
            with sqlite3.connect(restored) as data:
                self.assertEqual(27,data.execute("PRAGMA user_version").fetchone()[0])
                self.assertEqual("Unknown amphibian",data.execute(
                    "SELECT raw_identification FROM field_records WHERE id=42").fetchone()[0])
                self.assertEqual(("frog-rule","Herpetofauna"),data.execute(
                    "SELECT id,survey_profile FROM study_protocol_templates").fetchone())
                self.assertEqual("ok",data.execute("PRAGMA integrity_check").fetchone()[0])
        source.close()

    def test_migrations_are_additive_and_group_scoped(self):
        for version in VERSIONS:
            queries=migration_sql(version)
            self.assertFalse(any("DROP TABLE" in sql.upper() for sql in queries))
        self.assertIn("survey_profile TEXT",
            "\n".join(migration_sql(27)))
        self.assertIn("if(oldVersion<27)upgradeTwentySeven(d)",DB_SOURCE)
        self.assertIn("super(context,NAME,null,27)",DB_SOURCE)

if __name__=="__main__":
    unittest.main()
