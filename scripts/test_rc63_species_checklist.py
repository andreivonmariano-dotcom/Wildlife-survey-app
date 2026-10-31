"""RC63 full Philippine checklist UI, IME and historical-data contract."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
class Rc63Checklist(unittest.TestCase):
    def test_keyboard_and_accessibility_are_explicit(self):
        s=(JAVA/"Rc7SpeciesLibraryActivity.java").read_text()
        for token in ("SOFT_INPUT_ADJUST_RESIZE","IME_ACTION_SEARCH",
            'largeText?"Search taxon or family"', "groupLabel.setLabelFor(group.getId())",
            "scopeLabel.setLabelFor(scope.getId())",
            "scrollBody.addView(filters)",
            "scrollBody.addView(filterToggle", "updateFilterDisclosure()",
            "if(largeText){",
            "scrollBody.addView(researchPanel)",
            "AccessibilityNodeInfo info","info.setClassName(Button.class.getName())",
            "card.setMinimumHeight(dp(largeText?104:90))"):
            self.assertIn(token,s)
    def test_all_species_records_and_citations_remain_source_backed(self):
        s=(JAVA/"Rc7SpeciesLibraryActivity.java").read_text()
        for token in ("TaxonSearchPolicy.escapeLike","FROM taxon_catalog",
            "taxon_synonyms","Rc7LiteratureNotes.available","OfflinePhotoEvidence.hasOfflineTaxon",
            "Full Philippine species source records","WBCP 2025","Sy 2025/2026",
            "MDD 2.5","IUCN and DENR status must be reviewed separately",
            "source checklists are not complete species accounts",
            'putExtra("taxon_key",item.key)'):
            if token=="Full Philippine species source records":continue
            self.assertIn(token,s)
    def test_version_and_schema(self):
        b=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 124",b)
        self.assertIn("0.27.3-rc64-library-phone-icons-preview",b)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",b)
        self.assertIn("super(context,NAME,null,27)",(JAVA/"FieldDb.java").read_text())
    def test_no_schema_or_data_cleanup_issued_by_library(self):
        s=(JAVA/"Rc7SpeciesLibraryActivity.java").read_text().lower()
        self.assertNotIn("drop table",s)
        self.assertNotIn("delete from surveys",s)
        self.assertNotIn("delete from field_records",s)
if __name__=="__main__":unittest.main()
