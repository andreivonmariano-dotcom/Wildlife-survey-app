"""RC58 Library/Discover visual and source-data preservation contracts."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
class Rc58LibraryVisual(unittest.TestCase):
    def test_large_text_shelf_title_has_its_own_row(self):
        s=(JAVA/"LikasDiscoveryActivity.java").read_text()
        self.assertIn("final boolean stackedHeader=largeText()",s)
        self.assertIn("header.setOrientation(stackedHeader?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL)",s)
        self.assertIn("controls.addView(previous",s)
        self.assertIn("controls.addView(next",s)
        self.assertIn('searchInput.setHint(showingHabitat?"Search habitats":"Search species guides")',s)
        self.assertIn("Search illustrated wildlife guide accounts",s)
    def test_reference_routes_and_provenance_stay_present(self):
        d=(JAVA/"LikasDiscoveryActivity.java").read_text()
        for label in ("Status unverified","OfflinePhotoEvidence.galleryPhotoForTaxon",
            "TaxonDetailActivity.class","LikasHabitatProfileActivity.class",
            "Search complete species checklist","Habitat reference index and site records","statusIndex.get"):
            self.assertIn(label,d)
        hub=(JAVA/"LikasLibraryActivity.java").read_text()
        for label in ('"Wildlife guides"','"Habitat guides"','"Similar species"','"Photo comparisons"',
            '"Conservation evidence"','"Source checklists"','"Habitat associations"'):
            self.assertIn(label,hub)
    def test_version_and_data_identity(self):
        build=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(build.split("versionCode ",1)[1].split()[0]),125)
        self.assertIn("versionName \'0.27.3-",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,29)",(JAVA/"FieldDb.java").read_text())
if __name__=="__main__":unittest.main()
