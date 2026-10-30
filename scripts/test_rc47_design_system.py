"""RC47 design-system foundations: shared navigation, typography and card semantics."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def read(name):return (JAVA/name).read_text(encoding="utf-8")

class Rc47DesignSystem(unittest.TestCase):
    def test_core_tokens_are_a_single_source(self):
        tokens=read("LikasDesignSystem.java")
        for value in ("SPACE_XS=4","SPACE_SM=8","SPACE_MD=12","SPACE_LG=16",
                      "SPACE_XL=24","SPACE_XXL=32","SCREEN_GUTTER=16",
                      "RADIUS_CARD=19","MIN_TOUCH_DP=48","TEXT_SECTION=17",
                      "TEXT_CAPTION=13","useSingleColumn"):
            self.assertIn(value,tokens)
        self.assertIn("return LikasDesignSystem.useSingleColumn(availableWidthDp,fontScale)",
                      read("FieldLayoutPolicy.java"))

    def test_page_scaffold_is_shared_without_removing_hub_tools(self):
        ui=read("UiKit.java")
        self.assertIn("public static LinearLayout page(Activity a,String name,String parent",ui)
        self.assertIn("scroll.setFillViewport(true)",ui)
        self.assertIn("root.addView(navigationHeader(a,name,parent,onBack))",ui)
        for filename,label in [("LikasLibraryActivity.java","Library"),
                               ("LikasSettingsActivity.java","Settings")]:
            c=read(filename)
            self.assertIn(f'UiKit.page(this,"{label}","Home",this::finish,body)',c)
            self.assertIn("FieldVisuals.section(this,",c)
        library=read("LikasLibraryActivity.java")
        for name in ("LikasDiscoveryActivity.class","Rc9DifferentialActivity.class",
                     "Rc8PhotoComparisonActivity.class","HabitatSpeciesEvidenceActivity.class"):
            self.assertIn(name,library)
        settings=read("LikasSettingsActivity.java")
        for name in ("FieldBackupActivity.class","DataManagementActivity.class",
                     "ImportActivity.class","AuditActivity.class",
                     '"reduced_motion",checked'):
            self.assertIn(name,settings)

    def test_sections_and_controls_share_semantic_styles(self):
        visuals=read("FieldVisuals.java")
        self.assertIn("content.addView(UiKit.sectionTitle(a,title))",visuals)
        self.assertIn("UiKit.caption(a,caption)",visuals)
        ui=read("UiKit.java")
        self.assertIn("CONTROL_BORDER=Color.rgb(117,143,123)",ui)
        self.assertIn("AMBER=Color.rgb(145,85,20)",ui)
        self.assertIn('case "secondary":fill=Color.WHITE;stroke=CONTROL_BORDER;',ui)
        self.assertIn('case "quiet":fill=BG;stroke=CONTROL_BORDER;',ui)
        self.assertIn("LikasDesignSystem.MIN_TOUCH_DP",ui)
        portal=read("LikasPortalUi.java")
        self.assertIn("final int fill=tint;",portal)
        self.assertIn("UiKit.CONTROL_BORDER",portal)
        self.assertIn("background==Color.WHITE?UiKit.LEAF_MID:background",portal)
        self.assertIn('subtitle==null||subtitle.isEmpty()?"":". "+subtitle',portal)
        for art in ("likas_approved_fieldwork","likas_approved_birds_writhed_hornbill",
                    "likas_approved_herpetofauna_staurois_natator",
                    "likas_approved_mammals_rusa_marianna"):
            self.assertIn(art,portal)

    def test_scientific_identity_and_survey_exit_unchanged(self):
        build=(ROOT/"app/build.gradle").read_text(encoding="utf-8")
        self.assertIn("versionCode 108",build)
        self.assertIn("0.27.3-rc48-home-fieldwork-preview",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,27)",read("FieldDb.java"))
        recorder=read("MainActivity.java")
        for label in ('"Save Observation"','"End Survey"','"Review Records"',
                      '"Finish Complete"','"End Incomplete"','"Cancel Survey · Not Counted"'):
            self.assertIn(label,recorder)

if __name__=="__main__":unittest.main()
