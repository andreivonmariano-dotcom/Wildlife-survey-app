"""RC48: Home and Fieldwork use RC47 design tokens without changing survey semantics."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(name):return (JAVA/name).read_text(encoding="utf-8")

class Rc48HomeFieldwork(unittest.TestCase):
    def test_home_has_only_three_destinations_and_explicit_resume(self):
        welcome=src("WelcomeActivity.java")
        home=welcome.split("private void home(){",1)[1].split("private void phMapsHub(){",1)[0]
        self.assertIn("ApoHomeUi.header(this,",home)
        self.assertIn("ApoHomeUi.resume(this,",home)
        self.assertNotIn("LikasPortalUi.hero(",home)
        self.assertNotIn("LikasPortalUi.tile(",home)
        self.assertIn('Coming later: PHMaps · BioDiscover',home)
        for label in ('"BioTools"','"Living Atlas"'):
            self.assertIn(label,home)
        self.assertIn("LikasSettingsActivity.class",home)
        self.assertLess(home.index("ApoHomeUi.resume(this,"),
                        home.index('ApoHomeUi.module(this,"01","BioTools"'))
        self.assertNotIn("startSelected();",home)
    def test_active_fieldwork_is_not_labelled_a_new_survey(self):
        welcome=src("WelcomeActivity.java")
        hub=welcome.split("private void fieldworkHub(){",1)[1].split(
            "/** Short status caption",1)[0]
        self.assertIn('FieldVisuals.section(this,"Current survey",null)',hub)
        self.assertIn('FieldVisuals.section(this,"New survey",null)',hub)
        self.assertLess(hub.index('if(unfinished!=null)'),
                        hub.index('FieldVisuals.section(this,"Current survey"'))
        self.assertIn('UiKit.button(this,"Resume Survey","primary")',hub)
        for profile in ("Birds","Herpetofauna","Mammals"):
            self.assertIn('"'+profile+'"',hub)
        self.assertIn('private void fieldRecordsHub(){',hub)
        self.assertIn('HistoryActivity.class',hub)
        self.assertIn('private void analysisHub(){',hub)
        self.assertIn('DashboardActivity.class',hub)
        self.assertIn('surveyTile(main,"Birds"',hub)
        for target in ("SitesActivity.class","HistoryActivity.class",
                       "DashboardActivity.class","FieldProtocolsActivity.class"):
            self.assertIn(target,welcome)

    def test_survey_setup_has_shared_back_header_and_same_gps_dock(self):
        welcome=src("WelcomeActivity.java")
        setup=welcome.split("private void setup(String selected)",1)[1].split(
            "private void refreshStudyTitle()",1)[0]
        self.assertIn('UiKit.navigationHeader(this,profile+" survey","Surveys"',setup)
        self.assertIn("this::fieldworkHub",setup)
        self.assertIn("UiKit.fieldInput(this,input)",welcome)
        self.assertIn("styleSetupInput(entry)",setup)
        self.assertIn("UiKit.CONTROL_BORDER",welcome)
        self.assertIn("methodChoices.check(1000)",setup)
        self.assertIn('step("01","Survey method")',setup)
        self.assertIn('step("02","Project and location")',setup)
        self.assertIn('step("04","Begin survey")',setup)
        self.assertIn("setupFooter.setVisibility(View.VISIBLE)",setup)
        self.assertIn('setupStartButton.setOnClickListener(v->startSelected())',welcome)

    def test_recorder_forms_keep_native_input_types_and_uncertainty(self):
        recorder=src("MainActivity.java")
        self.assertIn("UiKit.fieldInput(this,e);return e;",recorder)
        self.assertIn("UiKit.fieldInput(this,taxonEntry)",recorder)
        self.assertIn('taxonEntry.setHint("Species")',recorder)
        self.assertIn("Math.max(56,FieldLayoutPolicy.actionHeightDp(",recorder)
        self.assertIn("getResources().getConfiguration().screenWidthDp",recorder)
        self.assertIn("boolean stackCaptureActions=FieldLayoutPolicy.stackPortalPair(",recorder)
        self.assertIn("detectRow.setOrientation(LinearLayout.VERTICAL)",recorder)
        self.assertIn("captureRow.setOrientation(LinearLayout.VERTICAL)",recorder)
        self.assertIn("markTime.setOnClickListener(v->markDetection())",recorder)
        self.assertIn("markButton.setOnClickListener(v->openCamera())",recorder)
        self.assertIn("quickCapture.setOnClickListener(v->{",recorder)
        self.assertIn("public static void fieldInput(Activity a,TextView input)",src("UiKit.java"))
        self.assertIn("input.setHintTextColor(MUTED)",src("UiKit.java"))

    def test_pinned_survey_lifecycle_and_release_identity_are_unchanged(self):
        recorder=src("MainActivity.java")
        for phrase in ('"Save Observation"','"Review Records"','"End Survey"',
                       '"Cancel Survey · Not Counted"'):
            self.assertIn(phrase,recorder)
        self.assertIn("root.addView(footer)",recorder)
        self.assertIn("endSurveyButton.setOnClickListener(v->finishPrompt())",recorder)
        self.assertIn("SurveyClosePolicy.resolve(",recorder)
        self.assertIn("root.getWindowVisibleDisplayFrame(visibleFrame)",recorder)
        welcome=src("WelcomeActivity.java")
        self.assertIn("new Intent(this,SurveyGpsActivity.class)",welcome)
        build=(ROOT/"app/build.gradle").read_text()
        self.assertGreaterEqual(int(__import__("re").search(r"versionCode\s+(\d+)",build).group(1)),109)
        self.assertIn("0.27.4-rc",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,29)",src("FieldDb.java"))

if __name__=="__main__":unittest.main()
