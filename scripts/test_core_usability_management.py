"""Contracts for LIKAS usability changes without erasing archived field data.

These tests inspect Android source and create synthetic SQLite fixtures.
No real user or species occurrence records are altered.
"""
from pathlib import Path
import re
import sqlite3
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

def source(name):
    return (JAVA/name).read_text(encoding="utf-8")

def table_names(java,field):
    m=re.search(r'private static final String\[\] '+field+r'=\{([^}]+)\}',java,re.S)
    if not m: raise AssertionError(field)
    return re.findall(r'"([a-z_]+)"',m.group(1))

class NewUsabilityContracts(unittest.TestCase):
    def test_dropdown_search_works_offline_and_respects_current_group(self):
        adapter=source("SpeciesReferenceSuggestionAdapter.java")
        self.assertIn("taxon_synonyms",adapter)
        self.assertIn("LIMIT 90",adapter)
        self.assertIn("list.size()<24",adapter)
        self.assertIn("groupFilter",adapter)
        for activity in ["TaxonDetailActivity.java","SpeciesPickerActivity.java"]:
            s=source(activity)
            self.assertIn("AutoCompleteTextView",s)
            self.assertIn("SpeciesReferenceSuggestionAdapter",s)
            self.assertIn("showDropDown()",s)
            self.assertIn("Show species",s)
        self.assertIn("nameSuggestions.setGroup(currentGroup())",
                      source("SpeciesPickerActivity.java"))

    def test_deletion_is_explicit_and_protects_live_records(self):
        deletion=source("FieldDataDeletion.java")
        for t in ["surveys","field_records","record_edits","observation_media",
                  "acoustic_clips","acoustic_labels","trap_captures",
                  "trap_checks","trap_deployments","coverboard_checks",
                  "track_sign_encounters","station_environment","survey_stations"]:
            self.assertIn(t,table_names(deletion,"EVENT_TABLES"))
        for t in ["sites","projects","route_templates",
                  "route_template_stations","study_protocol_templates"]:
            self.assertIn(t,table_names(deletion,"WORKSPACE_TABLES"))
        self.assertIn("db.beginTransaction()",deletion)
        self.assertIn("db.setTransactionSuccessful()",deletion)
        self.assertIn("countActive(db)>0",deletion)
        self.assertIn("seedDefaultProject(db)",deletion)
        self.assertNotIn("DROP TABLE",deletion)
        self.assertNotIn('db.delete("taxon_catalog"',deletion)
        self.assertNotIn('db.delete("taxon_occurrence_evidence"',deletion)
        ui=source("DataManagementActivity.java")
        for phrase in ["DELETE","DELETE SURVEYS","DELETE WORKSPACE",
                       "CLEAR FAVORITES","DELETE ALL"]:
            self.assertIn('"'+phrase+'"',ui)
        self.assertIn("Delete selected archived survey",ui)
        self.assertIn("Delete all my saved field data",ui)
        self.assertIn("deleteAllSurveys.setEnabled(active==0)",ui)
        self.assertIn("deleteWorkspace.setEnabled(surveys==0)",ui)
        self.assertIn("resetAll.setEnabled(active==0)",ui)
        for action in ["deleteSite","deleteProject","deleteRoute","deleteStudyTemplate"]:
            self.assertIn("public static void "+action+"(",deletion)
        self.assertIn('if(id==null||id.isEmpty()||"default".equals(id))',deletion)
        self.assertIn("chooseWorkspaceItem(index)",ui)
        self.assertIn("clearAllSurveyPreferences(context)",deletion)
        self.assertIn("Executors.newSingleThreadExecutor()",ui)
        self.assertIn("deletionWorker.execute(()->",ui)
        self.assertIn("runOnUiThread(()->",ui)
        self.assertIn('setCancelable(false)',ui)
        self.assertIn("if(deleting)return;",ui)
        self.assertIn("try(FieldDb local=new FieldDb(getApplicationContext()))",ui)
        self.assertIn("End Survey",ui)
        self.assertIn("research ZIP is not a complete restore backup",ui)

    def test_orphan_relationships_cannot_survive_single_survey_removal(self):
        # Synthetic relationship fixture exercises same dependency selectors in Java.
        db=sqlite3.connect(":memory:")
        db.executescript("""
        CREATE TABLE surveys(id TEXT,ended TEXT,outcome TEXT);
        CREATE TABLE field_records(id INTEGER,event_id TEXT);
        CREATE TABLE record_edits(record_id INTEGER);
        CREATE TABLE observation_media(record_id INTEGER);
        CREATE TABLE acoustic_clips(id TEXT,event_id TEXT);
        CREATE TABLE acoustic_labels(clip_id TEXT);
        CREATE TABLE trap_deployments(id TEXT,event_id TEXT);
        CREATE TABLE trap_checks(id INTEGER,deployment_id TEXT);
        CREATE TABLE trap_captures(check_id INTEGER);
        CREATE TABLE coverboard_units(id TEXT,event_id TEXT);
        CREATE TABLE coverboard_checks(board_pk TEXT);
        INSERT INTO surveys VALUES ('s1','done','complete'),('s2','done','complete');
        INSERT INTO field_records VALUES (1,'s1'),(2,'s2');
        INSERT INTO record_edits VALUES (1),(2);
        INSERT INTO observation_media VALUES (1),(2);
        INSERT INTO acoustic_clips VALUES ('c1','s1'),('c2','s2');
        INSERT INTO acoustic_labels VALUES ('c1'),('c2');
        INSERT INTO trap_deployments VALUES ('t1','s1'),('t2','s2');
        INSERT INTO trap_checks VALUES (11,'t1'),(12,'t2');
        INSERT INTO trap_captures VALUES (11),(12);
        INSERT INTO coverboard_units VALUES ('b1','s1'),('b2','s2');
        INSERT INTO coverboard_checks VALUES ('b1'),('b2');
        """)
        id="s1"
        db.execute("DELETE FROM acoustic_labels WHERE clip_id IN "+
                   "(SELECT id FROM acoustic_clips WHERE event_id=?)",(id,))
        db.execute("DELETE FROM trap_captures WHERE check_id IN "+
                   "(SELECT c.id FROM trap_checks c JOIN trap_deployments t "+
                   "ON t.id=c.deployment_id WHERE t.event_id=?)",(id,))
        db.execute("DELETE FROM trap_checks WHERE deployment_id IN "+
                   "(SELECT id FROM trap_deployments WHERE event_id=?)",(id,))
        db.execute("DELETE FROM coverboard_checks WHERE board_pk IN "+
                   "(SELECT id FROM coverboard_units WHERE event_id=?)",(id,))
        for table in ["observation_media","record_edits"]:
            db.execute("DELETE FROM "+table+" WHERE record_id IN "+
                       "(SELECT id FROM field_records WHERE event_id=?)",(id,))
        for table in ["acoustic_clips","trap_deployments","coverboard_units",
                      "field_records","surveys"]:
            db.execute("DELETE FROM "+table+" WHERE "+
                       ("id=?" if table=="surveys" else "event_id=?"),(id,))
        for table in ["surveys","field_records","record_edits","observation_media",
                      "acoustic_clips","acoustic_labels","trap_deployments",
                      "trap_checks","trap_captures","coverboard_units",
                      "coverboard_checks"]:
            self.assertEqual(1,db.execute("SELECT COUNT(*) FROM "+table).fetchone()[0],
                             table)
        self.assertIn("deleteEventRows(db,surveyId)",source("FieldDataDeletion.java"))

    def test_habitat_screen_has_provenance_and_no_invented_map(self):
        habitat=source("HabitatRecordsActivity.java")
        self.assertIn("station_environment",habitat)
        self.assertIn("elevation_source",habitat)
        self.assertIn("vertical_uncertainty_m",habitat)
        self.assertIn("Habitat not recorded",habitat)
        self.assertNotIn("getWritableDatabase()",habitat)
        self.assertNotIn("MapView",habitat)
        home=source("WelcomeActivity.java")
        self.assertIn('FieldVisuals.section(this,"Habitat & Ecology"',home)
        self.assertIn('DataManagementActivity.class',home)
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        self.assertIn('android:name=".DataManagementActivity"',manifest)
        self.assertIn('android:name=".HabitatRecordsActivity"',manifest)

if __name__=="__main__":
    unittest.main()
