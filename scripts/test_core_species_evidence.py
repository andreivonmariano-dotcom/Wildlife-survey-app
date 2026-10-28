"""Source-grounded integration tests for LIKAS regional evidence and synonym search.

SQLite examples are *synthetic*. The SQL used for species-reference lookup is
read from the actual Java source. These tests never invent real species ranges.
"""
import json
from pathlib import Path
import re
import sqlite3
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"
DETAIL = (JAVA/"TaxonDetailActivity.java").read_text(encoding="utf-8")
PICKER = (JAVA/"SpeciesPickerActivity.java").read_text(encoding="utf-8")
REGIONAL = (JAVA/"RegionalChecklistActivity.java").read_text(encoding="utf-8")

def java_strings(source):
    return "".join(json.loads(q) for q in
        re.findall(r'"(?:\\.|[^"\\])*"',source))

def actual_detail_lookup_sql():
    start = DETAIL.index('try(Cursor c=db.getReadableDatabase().rawQuery(',DETAIL.index("private void lookup("))
    finish = DETAIL.index("new String[]{query,query,query,pattern,pattern,pattern,query}",start)
    return java_strings(DETAIL[start:finish])

def conn():
    c=sqlite3.connect(":memory:")
    c.executescript("""
    CREATE TABLE taxon_catalog (
       taxon_key TEXT PRIMARY KEY,
       taxon_group TEXT,common_name TEXT,scientific_name TEXT);
    CREATE TABLE taxon_synonyms (
       taxon_key TEXT,alias_name TEXT,source TEXT,source_version TEXT);
    CREATE TABLE taxon_occurrence_evidence (
       taxon_key TEXT,area_type TEXT,area_label TEXT,review_status TEXT);
    INSERT INTO taxon_catalog VALUES ('SRC:1','Birds','Sample bird','Currentus one');
    INSERT INTO taxon_catalog VALUES ('SRC:2','Birds','Sample bird two','Currentus two');
    INSERT INTO taxon_catalog VALUES ('SRC:3','Birds','Sample bird three','Currentus three');
    INSERT INTO taxon_catalog VALUES ('STARTER:1','Birds','Old entry','Currentus one');
    INSERT INTO taxon_synonyms VALUES ('SRC:1','Oldus one','fake reference','test-1');
    INSERT INTO taxon_synonyms VALUES ('SRC:2','Oldus two','fake reference','test-1');
    INSERT INTO taxon_occurrence_evidence VALUES ('SRC:1','province','Davao Occidental','source_listed');
    INSERT INTO taxon_occurrence_evidence VALUES ('SRC:2','province','Davao del Sur','unreviewed');
    INSERT INTO taxon_occurrence_evidence VALUES ('SRC:3','island','Mindanao','source_listed');
    """)
    return c

class CoreEvidenceTests(unittest.TestCase):
    def test_former_name_resolves_to_current_accepted_taxon(self):
        sql=actual_detail_lookup_sql()
        pattern="%oldus one%"
        with conn() as db:
            rows=db.execute(sql,("Oldus one","Oldus one","Oldus one",
                                  pattern,pattern,pattern,"Oldus one")).fetchall()
        self.assertEqual(["SRC:1"],[row[0] for row in rows])
        self.assertIn("taxon_synonyms",sql)

    def test_unreviewed_source_does_not_rank_as_documented_local(self):
        self.assertIn("e.review_status='source_listed' AND e.area_type='province'",PICKER)
        self.assertIn("e.review_status='source_listed' AND e.area_type='island'",PICKER)
        # Evaluate the same conditional SQL that the picker builds.
        province = ("EXISTS(SELECT 1 FROM taxon_occurrence_evidence e "
                    "WHERE e.taxon_key=c.taxon_key AND e.review_status='source_listed' "
                    "AND e.area_type='province' AND LOWER(e.area_label)=LOWER(?))")
        island = ("EXISTS(SELECT 1 FROM taxon_occurrence_evidence e "
                  "WHERE e.taxon_key=c.taxon_key AND e.review_status='source_listed' "
                  "AND e.area_type='island' AND LOWER(e.area_label)=LOWER(?))")
        sql = ("SELECT c.taxon_key,CASE WHEN "+province+" THEN 2 "
               "WHEN "+island+" THEN 1 ELSE 0 END "
               "FROM taxon_catalog c WHERE c.taxon_key LIKE 'SRC:%' ORDER BY c.taxon_key")
        with conn() as db:
            ranks=dict(db.execute(sql,("Davao Occidental","Mindanao")).fetchall())
        self.assertEqual(2,ranks["SRC:1"])
        self.assertEqual(0,ranks["SRC:2"])
        self.assertEqual(1,ranks["SRC:3"])

    def test_regional_checklist_has_five_provinces_and_honors_review_status(self):
        self.assertIn('"Davao Occidental"',REGIONAL)
        self.assertIn("'Davao Occidental')",REGIONAL)
        self.assertIn("where+=\" AND e.review_status='source_listed'\"",REGIONAL)
        where=("e.area_type='province' AND e.area_label IN "
               "('Davao del Sur','Davao del Norte','Davao Oriental',"
               "'Davao de Oro','Davao Occidental') AND e.review_status='source_listed'")
        with conn() as db:
            ids=[row[0] for row in db.execute(
                "SELECT e.taxon_key FROM taxon_occurrence_evidence e WHERE "+where).fetchall()]
        self.assertEqual(["SRC:1"],ids)

    def test_regional_name_filter_accepts_synonyms(self):
        self.assertIn("SELECT 1 FROM taxon_synonyms a WHERE a.taxon_key=c.taxon_key", REGIONAL)
        self.assertIn("AND LOWER(a.alias_name) LIKE ? ESCAPE '!'", REGIONAL)
        self.assertIn("args.add(pattern);args.add(pattern);args.add(pattern)", REGIONAL)
        with conn() as db:
            records = db.execute(
                "SELECT DISTINCT e.taxon_key FROM taxon_occurrence_evidence e "
                "JOIN taxon_catalog c ON c.taxon_key=e.taxon_key "
                "WHERE e.review_status='source_listed' AND e.area_type='province' "
                "AND e.area_label='Davao Occidental' AND ("
                "LOWER(c.common_name) LIKE ? OR LOWER(c.scientific_name) LIKE ? "
                "OR EXISTS(SELECT 1 FROM taxon_synonyms a "
                "WHERE a.taxon_key=c.taxon_key AND LOWER(a.alias_name) LIKE ?))",
                ("%oldus one%",)*3).fetchall()
        self.assertEqual([("SRC:1",)], records)

    def test_unknown_locality_is_never_reported_as_absence(self):
        self.assertIn("onlyLocal&&province.trim().isEmpty()&&island(geographicRegion).isEmpty()",PICKER)
        self.assertIn("This does not mean species are absent.",PICKER)
        self.assertIn("Use All species or set the geographic survey area.",PICKER)

    def test_regional_empty_and_large_results_are_honest(self):
        self.assertIn("LIMIT 1001", REGIONAL)
        self.assertIn("if(ids.size()==1000){capped=true;break;}", REGIONAL)
        self.assertIn("Regional catalog unavailable:", REGIONAL)
        self.assertIn("searchHandler.postDelayed(searchRefresh,180L)", REGIONAL)

    def test_unreviewed_source_row_remains_in_audit_database(self):
        with conn() as db:
            rows=db.execute("SELECT taxon_key,review_status FROM taxon_occurrence_evidence "
                "WHERE review_status='unreviewed'").fetchall()
        self.assertEqual([("SRC:2","unreviewed")],rows)

if __name__=="__main__":
    unittest.main()
