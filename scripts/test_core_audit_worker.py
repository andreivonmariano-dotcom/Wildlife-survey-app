"""Read-only scientific audit must not freeze live UI or silently lose errors."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
ACTIVITY=(ROOT/"app/src/main/java/org/phwildlife/survey/AuditActivity.java").read_text(encoding="utf-8")
DATA=(ROOT/"app/src/main/java/org/phwildlife/survey/DataAudit.java").read_text(encoding="utf-8")

class ScientificAuditResponsivenessTest(unittest.TestCase):
    def test_separate_owned_readonly_database_worker(self):
        self.assertIn("Executors.newSingleThreadExecutor()", ACTIVITY)
        self.assertIn("auditWorker.execute(()->", ACTIVITY)
        self.assertIn("try(FieldDb local=new FieldDb(getApplicationContext()))", ACTIVITY)
        self.assertIn("DataAudit.run(getApplicationContext(),local.getReadableDatabase())", ACTIVITY)
        self.assertIn("runOnUiThread(()->", ACTIVITY)

    def test_ui_feedback_and_clean_shutdown(self):
        self.assertIn("if(auditRunning)return;", ACTIVITY)
        self.assertIn("refreshButton.setEnabled(false)", ACTIVITY)
        self.assertIn("refreshButton.setEnabled(true)", ACTIVITY)
        self.assertIn("auditWorker.shutdownNow()", ACTIVITY)
        self.assertIn('\"AUDIT_UNAVAILABLE\"', ACTIVITY)

    def test_audit_has_no_observation_write_paths(self):
        self.assertNotIn("getWritableDatabase()", ACTIVITY)
        self.assertIn("PRAGMA quick_check", DATA)
        self.assertNotIn("UPDATE field_records", DATA)
        self.assertNotIn("DELETE FROM field_records", DATA)

if __name__=="__main__":
    unittest.main()
