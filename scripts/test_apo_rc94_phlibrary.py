"""RC94 PHLibrary navigation and source-boundary regression checks."""
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"app/src/main/java/org/phwildlife/survey"

def read(name):
    return (SRC/name).read_text(encoding="utf-8")

class PHLibraryScreenContract(unittest.TestCase):
    def test_full_source_checklist_direct_from_hub(self):
        hub=read("LikasLibraryActivity.java")
        self.assertIn('"PHLibrary","Home"',hub)
        self.assertIn('"Search species checklist"',hub)
        self.assertIn("Rc7SpeciesLibraryActivity.class",hub)
        self.assertIn("LikasDiscoveryActivity.class",hub)
        self.assertIn('"view",view',hub)
        self.assertLess(hub.index('"Search species checklist"'),
                        hub.index('"Wildlife guides"'))
        self.assertIn("island",hub.lower())
        self.assertNotIn("Full Philippine species checklist",hub)

    def test_discovery_separates_account_scope_from_source_catalog(self):
        source=read("LikasDiscoveryActivity.java")
        self.assertIn("Selected illustrated accounts",source)
        self.assertIn('"Search complete species checklist"',source)
        self.assertIn("Rc7SpeciesLibraryActivity.class",source)
        self.assertIn('"Habitat reference index and site records"',source)
        self.assertIn("HabitatRecordsActivity.class",source)
        self.assertNotIn('"Full Philippine species checklist"',source)
        self.assertNotIn('"Species ●"',source)

    def test_conservation_authorities_are_not_blended_into_priority(self):
        source=read("LikasDiscoveryActivity.java")
        self.assertNotIn("Math.max(categoryLevel",source)
        self.assertNotIn("level(status)",source)
        self.assertIn('"iucn","denr"',source)
        self.assertIn('compareToIgnoreCase(',source)
        self.assertIn('"Cited conservation"',source)

    def test_large_text_search_and_tabs_have_reversible_state(self):
        source=read("LikasDiscoveryActivity.java")
        self.assertIn("private void switchView(boolean habitat)",source)
        self.assertIn('out.putString("speciesSearchQuery"',source)
        self.assertIn('out.putString("habitatSearchQuery"',source)
        self.assertIn("matchingResults(matches,true)",source)
        self.assertIn("matchingResults(matches,false)",source)
        self.assertIn('setContentDescription("Illustrated species guides"',source)
        self.assertIn('setContentDescription("Illustrated habitat guides"',source)

    def test_app_identity_biology_and_schema_remain(self):
        gradle=(ROOT/"app/build.gradle").read_text(encoding="utf-8")
        self.assertIn("versionCode 155",gradle)
        self.assertIn("0.27.3-rc95-biotools-navigation",gradle)
        self.assertIn("org.phwildlife.survey.beta08",gradle)
        self.assertIn("super(context,NAME,null,29)",read("FieldDb.java"))
        self.assertIn('manifest.put("sqliteSchema",snapshotSchema)',
                      read("FieldBackupArchive.java"))
        self.assertIn("SurveyClosePolicy.resolve(",read("MainActivity.java"))

if __name__=="__main__":
    unittest.main()
