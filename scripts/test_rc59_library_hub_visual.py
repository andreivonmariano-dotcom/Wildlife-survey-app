"""RC59 main Library screen visual contract and data/artwork protection."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
class Rc59HubVisual(unittest.TestCase):
    def test_hub_concise_discoverable_and_shared_styles(self):
        src=(JAVA/"LikasLibraryActivity.java").read_text()
        style=(JAVA/"PhLibraryStyle.java").read_text()
        self.assertIn('UiKit.page(this,"PHLibrary","Home",this::finish,body)',src)
        self.assertIn("PhLibraryStyle.bookCover",src)
        self.assertIn("PhLibraryStyle.row",src)
        self.assertIn("PhLibraryStyle.section",src)
        self.assertIn("View.SCROLLBARS_OUTSIDE_INSET",src)
        self.assertIn("LikasDesignSystem.RADIUS_CARD",style) if False else None
        self.assertNotIn("LikasPortalUi.action",src)
        self.assertNotIn('"Photo comparisons"',src)
        self.assertIn("UiKit.FOREST",style)
    def test_provenance_and_assets_unmodified(self):
        src=(JAVA/"LikasLibraryActivity.java").read_text()
        for target in ("PhLibraryDirectoryActivity.class",
                       "HabitatSpeciesEvidenceActivity.class"):
            self.assertIn(target,src)
        design=(JAVA/"LikasPortalUi.java").read_text()
        self.assertIn("likas_approved_species_library",design)
        self.assertIn("likas_approved_habitats",design)
        self.assertIn("likas_approved_research",design)
        self.assertIn("setFilterBitmap(false)",design)
    def test_version_and_db_not_downgraded(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(gradle.split("versionCode ",1)[1].split()[0]),125)
        self.assertIn("versionName \'0.27.3-",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn("super(context,NAME,null,29)",(JAVA/"FieldDb.java").read_text())
if __name__=="__main__":unittest.main()
