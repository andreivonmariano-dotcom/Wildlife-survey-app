"""RC60 main Library hierarchy, pixel semantic shield and consistent navigation."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
class Rc60VisualDecisions(unittest.TestCase):
    def test_new_generated_icons_and_locked_approved_set_stay_separate(self):
        portal=(JAVA/"LikasPortalUi.java").read_text()
        library=(JAVA/"LikasLibraryActivity.java").read_text()
        self.assertIn("R.drawable.likas_library_conservation_display",portal)
        self.assertIn("R.drawable.likas_library_habitat_links_display",portal)
        self.assertIn('"Species Atlas"',library)
        self.assertIn('"Habitat Atlas"',library)
        self.assertNotIn("class PixelShield",portal)
        self.assertIn("likas_approved_species_library",portal)
        self.assertIn("likas_approved_research",portal)
    def test_library_title_section_hierarchy_is_local_only(self):
        library=(JAVA/"LikasLibraryActivity.java").read_text()
        style=(JAVA/"PhLibraryStyle.java").read_text()
        self.assertIn("PhLibraryPhotoHero.create",library)
        self.assertIn("View tileGap",library)
        self.assertIn("tile(body,",library)
        self.assertIn("new ScrollView(this)",library)
        self.assertIn("sans-serif-medium",style)
        self.assertNotIn("FieldVisuals.section(this",library)
    def test_main_destinations_use_same_open_cue_and_original_routing(self):
        portal=(JAVA/"LikasPortalUi.java").read_text()
        library=(JAVA/"LikasLibraryActivity.java").read_text()
        self.assertIn("public static View libraryFeature(",portal)
        self.assertIn("PhLibraryPhotoHero.create",library)
        self.assertNotIn("LikasPortalUi.libraryFeature(this",library)
        for target in ["PhLibraryDirectoryActivity.class",
                       "HabitatSpeciesEvidenceActivity.class"]:
            self.assertIn(target,library)
        self.assertNotIn("Rc8PhotoComparisonActivity.class",library)
        build=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(build.split("versionCode ",1)[1].split()[0]),125)
        self.assertIn("versionName \'0.27.4-",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,29)",(JAVA/"FieldDb.java").read_text())
if __name__=="__main__":unittest.main()
