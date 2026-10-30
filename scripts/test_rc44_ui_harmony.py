"""RC44 presentation-only audit: rounded icons, restrained color, and optional motion."""
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"app/src/main/java/org/phwildlife/survey"
def read(name):
    return (SRC/name).read_text(encoding="utf-8")

class Rc44UiHarmony(unittest.TestCase):
    def test_rounding_keeps_locked_art_unchanged(self):
        portal=read("LikasPortalUi.java")
        self.assertIn("frame.setClipToOutline(true)",portal)
        self.assertIn("frog.setClipToOutline(true)",portal)
        self.assertIn("setFilterBitmap(false)",portal)
        for size in ("64,36","52,30","50,28"):
            self.assertIn(size,portal)
        self.assertIn("approvedIconResource(String symbol)",portal)
        self.assertIn("new Glyph(a,symbol,glyphColor)",portal)

    def test_shared_forest_palette_and_semantic_warnings(self):
        kit=read("UiKit.java")
        portal=read("LikasPortalUi.java")
        for name in ("DEEP","FOREST","POND","MUTED","LEAF_LIGHT","LEAF_MID","HIGHLIGHT","BORDER"):
            self.assertIn("public static final int "+name+"=",kit)
        self.assertIn("public static final int CANOPY=UiKit.INK",portal)
        self.assertIn("public static final int DEEP=UiKit.DEEP",portal)
        self.assertIn("new int[]{DEEP,CANOPY,UiKit.FOREST}",portal)
        self.assertNotIn("Color.rgb(49,116,124)",portal)
        for important in ("BLUE","AMBER"):
            self.assertIn("public static final int "+important+"=",kit)

    def test_compact_three_group_fieldwork_and_relevant_icons(self):
        welcome=read("WelcomeActivity.java")
        self.assertIn('surveyTile(first,"Birds"',welcome)
        self.assertIn('surveyTile(first,"Herpetofauna"',welcome)
        self.assertIn('surveyTile(second,"Mammals"',welcome)
        self.assertIn('"Mammals".equals(title)',welcome)
        self.assertIn('LikasPortalUi.action(this,title,detail,art,tap)',welcome)
        self.assertNotIn('surveyTile(second,"Custom"',welcome)
        settings=read("LikasSettingsActivity.java")
        self.assertIn('"Data check","Review records","reports"',settings)
        library=read("LikasLibraryActivity.java")
        self.assertIn('LikasPortalUi.tile(',library)

    def test_motion_is_nonblocking_and_accessible(self):
        motion=read("UiMotion.java")
        welcome=read("WelcomeActivity.java")
        self.assertIn('getBoolean("reduced_motion",false)',motion)
        self.assertIn("ValueAnimator.areAnimatorsEnabled()",motion)
        self.assertIn("fontScale<1.60f",motion)
        self.assertIn(".setDuration(155)",motion)
        self.assertIn("UiMotion.enter(this,main)",welcome)
        self.assertIn("UiMotion.hide(moreTools)",welcome)
        self.assertIn("UiMotion.reveal(this,moreTools)",welcome)
        self.assertIn("UiMotion.reveal(this,studyOptions)",welcome)
        self.assertIn("UiMotion.hide(studyOptions)",welcome)
        self.assertIn('setupStartButton.setOnClickListener(v->startSelected())',welcome)
        self.assertIn('Resume Survey',welcome)

    def test_preserve_package_database_and_survey_contract(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 106",gradle)
        self.assertIn("versionName '0.27.3-rc46-field-layout-hierarchy-preview'",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        db=read("FieldDb.java")
        self.assertIn("super(context,NAME,null,27)",db)
        history=read("HistoryActivity.java")
        self.assertIn("SurveyOutcomeSql.RESOLVED",history)

if __name__=="__main__":
    unittest.main()
