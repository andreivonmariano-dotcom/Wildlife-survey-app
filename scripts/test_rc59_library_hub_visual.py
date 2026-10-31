"""RC59 main Library screen visual contract and data/artwork protection."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
class Rc59HubVisual(unittest.TestCase):
    def test_hub_concise_discoverable_and_shared_styles(self):
        src=(JAVA/"LikasLibraryActivity.java").read_text()
        for phrase in ('"Species","Illustrated accounts"','"Habitats","Habitat profiles"',
            '"Similar species","Compare traits","research"','"Conservation","Dated assessments"',
            '"Habitat links","Cited associations"','"Photos","Offline images"',
            '"Sources","Checklists"'):
            self.assertIn(phrase,src)
        self.assertIn("UiKit.page(this,\"Library\",\"Home\",this::finish,body)",src)
        self.assertIn("librarySection(\"Species & habitats\"",src)
        self.assertIn("librarySection(\"Reference tools\"",src)
        self.assertIn("getResources().getConfiguration().screenWidthDp<600",src)
        self.assertIn("libraryScroll.setPadding(0,0,dp(8),0)",src)
        self.assertIn("View.SCROLLBARS_OUTSIDE_INSET",src)
        self.assertIn("LikasPortalUi.libraryFeature(this",src)
        self.assertIn("LikasPortalUi.action(this",src)
    def test_provenance_and_assets_unmodified(self):
        src=(JAVA/"LikasLibraryActivity.java").read_text()
        for target in ("LikasDiscoveryActivity.class","Rc9DifferentialActivity.class",
            "Rc8PhotoComparisonActivity.class","Rc24ConservationDashboardActivity.class",
            "CatalogActivity.class","HabitatSpeciesEvidenceActivity.class"):
            self.assertIn(target,src)
        design=(JAVA/"LikasPortalUi.java").read_text()
        self.assertIn("likas_approved_species_library",design)
        self.assertIn("likas_approved_habitats",design)
        self.assertIn("likas_approved_research",design)
        self.assertIn("setFilterBitmap(false)",design)
    def test_version_and_db_not_downgraded(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 123",gradle)
        self.assertIn("0.27.3-rc63-species-checklist-preview",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,27)",(JAVA/"FieldDb.java").read_text())
if __name__=="__main__":unittest.main()
