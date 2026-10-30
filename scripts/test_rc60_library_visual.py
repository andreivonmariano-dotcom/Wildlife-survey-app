"""RC60 main Library hierarchy, pixel semantic shield and consistent navigation."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
class Rc60VisualDecisions(unittest.TestCase):
    def test_new_generated_icons_and_locked_approved_set_stay_separate(self):
        portal=(JAVA/"LikasPortalUi.java").read_text()
        library=(JAVA/"LikasLibraryActivity.java").read_text()
        self.assertIn("R.drawable.likas_generated_conservation",portal)
        self.assertIn("R.drawable.likas_generated_habitat_links",portal)
        self.assertIn('"Conservation","Dated assessments","library-conservation"',library)
        self.assertIn('"Habitat links","Cited associations","library-habitat-links"',library)
        self.assertNotIn("class PixelShield",portal)
        self.assertIn("likas_approved_species_library",portal)
        self.assertIn("likas_approved_research",portal)
    def test_library_title_section_hierarchy_is_local_only(self):
        library=(JAVA/"LikasLibraryActivity.java").read_text()
        self.assertIn("private LinearLayout librarySection(",library)
        self.assertIn("FieldVisuals.section(this,title,caption)",library)
        self.assertIn("heading.setTextSize(14)",library)
        self.assertIn("heading.setLetterSpacing(.06f)",library)
        self.assertIn("Typeface.create(\"sans-serif-medium\",Typeface.NORMAL)",library)
        self.assertIn('UiKit.page(this,"Library","Home",this::finish,body)',library)
        self.assertIn("pageTitle.setTextSize(LikasDesignSystem.TEXT_PAGE)",library)
        self.assertIn('librarySection("Species & habitats"',library)
        self.assertIn('librarySection("Reference tools"',library)
    def test_main_destinations_use_same_open_cue_and_original_routing(self):
        portal=(JAVA/"LikasPortalUi.java").read_text()
        library=(JAVA/"LikasLibraryActivity.java").read_text()
        self.assertIn("public static View libraryFeature(",portal)
        self.assertIn("LinearLayout panel=(LinearLayout)action(a,title,subtitle,icon,open)",portal)
        self.assertIn("panel.setMinimumHeight(dp(a,LikasDesignSystem.HOME_CARD_MIN_DP))",portal)
        self.assertIn("LikasPortalUi.libraryFeature(this",library)
        self.assertEqual(library.count("discover(body,"),2)
        for target in ["LikasDiscoveryActivity.class","Rc9DifferentialActivity.class",
                       "Rc8PhotoComparisonActivity.class","Rc24ConservationDashboardActivity.class",
                       "CatalogActivity.class","HabitatSpeciesEvidenceActivity.class"]:
            self.assertIn(target,library)
        build=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 121",build)
        self.assertIn("0.27.3-rc61-library-icon-preview",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,27)",(JAVA/"FieldDb.java").read_text())
if __name__=="__main__":unittest.main()
