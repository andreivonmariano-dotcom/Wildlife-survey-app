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
        self.assertIn('searchInput.setHint(largeText()?"Search":"Find name or habitat")',s)
        self.assertIn("Search illustrated offline species and habitat accounts",s)
    def test_reference_routes_and_provenance_stay_present(self):
        d=(JAVA/"LikasDiscoveryActivity.java").read_text()
        for label in ("Status unverified","OfflinePhotoEvidence.galleryPhotoForTaxon",
            "TaxonDetailActivity.class","LikasHabitatProfileActivity.class",
            "Full Philippine species checklist","All habitats","statusIndex.get"):
            self.assertIn(label,d)
        hub=(JAVA/"LikasLibraryActivity.java").read_text()
        for label in ('"Species"','"Habitats"','"Similar species"','"Photos"',
            '"Conservation"','"Sources"','"Habitat links"'):
            self.assertIn(label,hub)
    def test_version_and_data_identity(self):
        build=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 119",build)
        self.assertIn("0.27.3-rc59-library-hub-preview",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,27)",(JAVA/"FieldDb.java").read_text())
if __name__=="__main__":unittest.main()
