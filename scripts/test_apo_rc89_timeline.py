"""RC89 live timeline: saved/draft boundary and audited correction provenance."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"app/src/main/java/org/phwildlife/survey"
def read(name):return (SRC/name).read_text(encoding="utf-8")

class TimelineContracts(unittest.TestCase):
    def test_saved_only_read_model_is_event_scoped(self):
        query=read("ObservationTimeline.java")
        self.assertIn("field_records WHERE event_id=? AND deleted_at IS NULL",query)
        self.assertIn("record_edits e WHERE e.record_id=r.id AND e.action='edit'",query)
        self.assertIn("r.event_id=? AND r.deleted_at IS NULL",query)
        self.assertIn("ORDER BY r.detected_at DESC,r.id DESC LIMIT ?",query)
        self.assertIn("Math.min(limit,8)",query)
        self.assertNotIn("INSERT INTO",query)
        self.assertNotIn("UPDATE field_records",query)
    def test_ui_has_distinct_unsaved_and_saved_status(self):
        code=read("MainActivity.java")
        for part in ('Saved timeline · 0','Draft · No unsaved observation',
                     'Unsaved draft · Not counted in timeline',
                     'ObservationTimeline.load(', 'timelineRows.setVisibility(',
                     'refreshTimeline();','openSurveyReview(record.id)',
                     '.putExtra("event_id",eventId)',
                     'review.putExtra("focus_record_id",focusedRecordId)'):
            self.assertIn(part,code)
        self.assertLess(code.index('content.addView(photoRow);'),
                        code.index('content.addView(timelineToggle);'))
        self.assertIn('toast(distance==null?"Observation saved',code)
        self.assertIn('refreshTimeline();\n        toast(distance',code)
    def test_audited_edit_requires_reason_and_preserves_taxon_provenance(self):
        h=read("HistoryActivity.java")
        for part in ('focus_record_id','AND event_id=?',
                     'Reason for correction (required)',
                     'why.length()<5','values.putNull("taxon_key")',
                     'values.putNull("taxon_source_version")',
                     'values.put("name_source","user_corrected")',
                     'int changed=writable.update("field_records"',
                     'auditWrite(writable,id,"edit",before,after,why)',
                     'writable.beginTransaction()','writable.setTransactionSuccessful()'):
            self.assertIn(part,h)
        assert h.index('writable.update("field_records"')<h.index('auditWrite(writable,id,"edit"')
        self.assertIn('CREATE TABLE IF NOT EXISTS record_edits(',read("FieldDb.java"))
        self.assertIn('super(context,NAME,null,29)',read("FieldDb.java"))
    def test_existing_identity_and_survey_dispositions(self):
        g=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 176",g)
        self.assertIn("0.27.4-rc116-localities-research-preview",g)
        self.assertIn("org.phwildlife.survey.beta08",g)
        m=read("MainActivity.java")
        for token in ('Finish Complete','End Incomplete','Cancel Survey · Not Counted',
                      'SurveyClosePolicy.resolve(','if(hasUnsavedObservation())'):
            self.assertIn(token,m)

if __name__=="__main__": unittest.main()
