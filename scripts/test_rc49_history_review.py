"""RC49 read-only review and History layout contracts. Evidence and audit safety."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name):return (JAVA/name).read_text(encoding="utf-8")

class Rc49HistoryReview(unittest.TestCase):
    def test_history_has_search_clear_and_outcome_filters(self):
        history=source("HistoryActivity.java")
        self.assertIn("OUTCOME_SQL=SurveyOutcomeSql.RESOLVED",history)
        self.assertIn('"All outcomes","Complete","Incomplete","Canceled / Not counted","Active"',history)
        self.assertIn('root.addView(UiKit.caption(this,"Survey status"))',history)
        self.assertIn("UiKit.fieldInput(this,search)",history)
        self.assertIn('clear.setContentDescription("Clear survey search")',history)
        self.assertIn('clear.setOnClickListener(v->search.setText(""))',history)
        self.assertIn("searchHandler.postDelayed(pendingSearch,220L)",history)
        self.assertIn("surveyOffset+=PAGE_SIZE",history)

    def test_read_only_detail_preserves_stored_values_and_missingness(self):
        history=source("HistoryActivity.java")
        self.assertIn('"View recorded details","Edit observation"',history)
        self.assertIn("if(index==0)showRecordDetails(id,original)",history)
        self.assertIn("private static String observed(JSONObject source,String key)",history)
        self.assertIn('if(!source.has(key)||source.isNull(key))return "Not recorded"',history)
        self.assertIn("private void showRecordDetails(long id,JSONObject source)",history)
        for column in ("raw_identification","scientific_name","taxon_group",
                       "individual_count","detection_method","detected_at",
                       "horizontal_m","distance_method","identification_confidence",
                       "identification_review_note","habitat","behavior","notes",
                       "observer_lat","observer_lon","gps_accuracy_m"):
            self.assertIn(f'observed(source,"{column}")',history)
        self.assertIn('"Current recorded identification"',history)
        self.assertNotIn('"Original field identification"',history)
        detail=history.split("private void showRecordDetails(",1)[1].split(
            "private void viewPhotos(",1)[0]
        self.assertNotIn("getWritableDatabase()",detail)
        self.assertNotIn("writable.update(",detail)

    def test_event_context_and_zero_detection_are_not_mutated(self):
        history=source("HistoryActivity.java")
        self.assertIn("LinearLayout summary=column()",history)
        self.assertIn("summary.setBackground(UiKit.surface(this))",history)
        self.assertIn('"No detections recorded. The survey event remains saved."',history)
        self.assertIn('FieldVisuals.section(this,"Observations"',history)
        self.assertIn("card.setContentDescription(c.getString(1)",history)
        self.assertIn('r.deleted_at IS NULL ORDER BY',history)
        self.assertIn("observationOffset+=PAGE_SIZE",history)

    def test_edit_and_soft_delete_retain_original_audit_snapshots(self):
        history=source("HistoryActivity.java")
        self.assertIn("UiKit.fieldInput(this,v)",history)
        self.assertIn("notes.setSingleLine(false);notes.setMinLines(3)",history)
        self.assertIn("InputType.TYPE_TEXT_FLAG_MULTI_LINE",history)
        self.assertIn('"Reason for correction (recommended)"',history)
        self.assertIn('auditWrite(writable,id,"edit",before,after,reason.getText().toString())',history)
        self.assertIn('auditWrite(dbw,id,"soft_delete",before,after,"User deleted record")',history)
        self.assertIn('db.insertOrThrow("record_edits",null,v)',history)
        self.assertIn("writable.beginTransaction()",history)
        self.assertIn("writable.setTransactionSuccessful()",history)
        self.assertIn("dbw.beginTransaction()",history)

    def test_edit_history_displays_previous_and_updated_full_records(self):
        history=source("HistoryActivity.java")
        self.assertIn('SELECT edited_at,action,COALESCE(reason,\'\'),previous_json,current_json',history)
        self.assertIn('msg.append("Before:\\n")',history)
        self.assertIn('.append("\\nAfter:\\n")',history)
        self.assertIn("history.setTextIsSelectable(true)",history)
        self.assertIn('new AlertDialog.Builder(this).setTitle("Record edit history")',history)

    def test_version_and_data_compatibility(self):
        build=(ROOT/"app/build.gradle").read_text(encoding="utf-8")
        self.assertGreaterEqual(int(__import__("re").search(r"versionCode\s+(\d+)",build).group(1)),109)
        self.assertIn("0.27.3-rc",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,27)",source("FieldDb.java"))
        self.assertIn('endSurveyButton=UiKit.button(this,"End Survey","outline-danger")',
                      source("MainActivity.java"))
        portal=source("LikasPortalUi.java")
        for icon in ("likas_approved_birds_writhed_hornbill",
                     "likas_approved_herpetofauna_staurois_natator",
                     "likas_approved_mammals_rusa_marianna"):
            self.assertIn(icon,portal)

if __name__=="__main__":
    unittest.main()
