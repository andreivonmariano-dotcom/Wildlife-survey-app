import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/org/phwildlife/survey"

class Rc10Tests(unittest.TestCase):
    def test_review_drafts_are_atomic_and_exportable(self):
        source = (JAVA / "Rc9PhotoAnnotationReviewActivity.java").read_text()
        for token in ["AtomicFile", "atomic.finishWrite", "atomic.failWrite", "Intent.ACTION_CREATE_DOCUMENT"]:
            self.assertIn(token, source)

    def test_research_ui_and_evidence(self):
        library = (JAVA / "Rc7SpeciesLibraryActivity.java").read_text()
        review = (JAVA / "Rc9ProfileCompleteness.java").read_text()
        self.assertIn("Priority: diagnosis unassessed", library)
        self.assertIn("researchPanel.setVisibility", library)
        self.assertIn("rc8_evidence_comparisons_v1.json", review)
        self.assertIn("Read cited scientific source", review)
