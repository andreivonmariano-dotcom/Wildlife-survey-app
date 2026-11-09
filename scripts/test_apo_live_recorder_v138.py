"""APO RC78 live recorder layout, safe exit, and emulator test contracts."""
import re
import unittest
from pathlib import Path
BASE=Path(__file__).resolve().parents[1]
JAVA=BASE/"app/src/main/java/org/phwildlife/survey"
def source(name): return (JAVA/name).read_text(encoding="utf-8")

class LiveRecorderRC78(unittest.TestCase):
    def test_session_detection_and_observation_sections_stay_ordered(self):
        s=source("MainActivity.java")
        self.assertLess(s.index('UiKit.sectionTitle(this,"Sampling session")'),
                        s.index('UiKit.sectionTitle(this,"Detection tools")'))
        self.assertLess(s.index('UiKit.sectionTitle(this,"Detection tools")'),
                        s.index('FieldVisuals.section(this,"Observation"'))
        self.assertIn('FieldQuickActions.resolve(profile,method',s)
        self.assertIn('FieldQuickActions.choices(profile,method)',s)
        self.assertIn('SurveyHudReader(this)',s)
        self.assertIn('now-lastProgressFetchMs>=6000L',s)

    def test_save_is_explained_and_exit_pinned_when_keyboard_opens(self):
        s=source("MainActivity.java")
        for check in ('saveEligibilityView=UiKit.caption(this,"")',
                      "saveEligibilityView.setAccessibilityLiveRegion(",
                      "private void refreshSaveEligibility()",
                      "Start a point count at its station",
                      "Start a sampling station visit",
                      "Resume sampling to save",
                      "FieldLayoutPolicy.stackPortalPair(",
                      "if(stackDock)footerOptions.setOrientation(LinearLayout.VERTICAL)",
                      'endSurveyButton=UiKit.button(this,"End Survey","outline-danger")',
                      'endSurveyButton.setOnClickListener(v->finishPrompt())',
                      "top.setVisibility(keyboardShown?View.GONE:View.VISIBLE)"):
            self.assertIn(check,s)
        self.assertIn('footer.addView(saveButton,new LinearLayout.LayoutParams(-1,-2))',s)
        self.assertIn('endParams.topMargin=dp(LikasDesignSystem.SPACE_SM)',s)

    def test_end_survey_accounts_for_more_than_taxon_text(self):
        s=source("MainActivity.java")
        closure=s.split("private boolean hasUnsavedObservation(){",1)[1].split(
            "private void finishPrompt()",1)[0]
        for entry in ("taxonEntry","notesEntry","distanceEntry","habitatEntry",
                      "behaviorEntry","pendingPhoto","markedAt","count!=1",
                      "confidenceSpinner","evidenceSpinner","lifeStageSpinner",
                      "distanceSourceSpinner","firstPositionSpinner","movementSpinner"):
            self.assertIn(entry,closure)
        self.assertIn("if(hasUnsavedObservation())",s)
        self.assertIn("discardUnfinishedObservation()",s)
        self.assertIn("endScroll.addView(actions)",s)
        self.assertIn("SurveyClosePolicy.resolve(",s)
        for label in ("Finish Complete","End Incomplete","Cancel Survey · Not Counted"):
            self.assertIn('"'+label+'"',s)

    def test_existing_survey_data_semantics_and_identity_preserved(self):
        s=source("MainActivity.java")
        self.assertIn('endSurveyButton.setEnabled(active)',s)
        self.assertIn("if(surveyPaused)endPause()",s)
        self.assertIn("fieldScroll.addView(content)",s)
        self.assertIn("recorded_at",source("FieldDb.java"))
        b=(BASE/"app/build.gradle").read_text(encoding="utf-8")
        self.assertRegex(b,r"versionCode (13[89]|(14[0-9]|15[0-9]|16[0-8]))")
        self.assertRegex(b,r"0\.27\.3-rc(78-apo-survey-recorder|79-apo-survey-flow|80-apo-survey-hud-stations|81-apo-survey-compact-320|82-apo-survey-setup|83-apo-survey-gps|84-apo-habitats-checklist|85-apo-recorder-species|86-apo-interactive-recorder|87-apo-habitat-shortlist|88-apo-detection-first|89-apo-live-timeline|90-apo-method-panels|91-apo-method-review|92-apo-closure-integrity|93-apo-schema29-backup-qa|94-phlibrary-preview|95-biotools-navigation|96-phlibrary-reader|97-phlibrary-visual|98-phlibrary-editorial|99-phlibrary-habitats|100-integrated-habitats|101-phlibrary-book-v182|102-startup-centered-motion|103-phlibrary-2-native|104-phlibrary-large-text|105-phlibrary-clean-atlas|106-phlibrary-polish|107-habitat-reader-saved|108-species-media-daily)")
        self.assertIn("org.phwildlife.survey.beta08",b)
        self.assertIn("super(context,NAME,null,29)",source("FieldDb.java"))

    def test_real_emulator_test_and_automated_capture_configured(self):
        instrumentation=(BASE/"app/src/androidTest/java/org/phwildlife/survey/RecorderUiEmulatorTest.java").read_text()
        workflow=(BASE/".github/workflows/apo-recorder-v138-emulator-qa.yml").read_text()
        self.assertIn('recorderHasPersistentSaveReviewAndEndSurvey()',instrumentation)
        self.assertIn('assertTrue("End Survey must stay visible"',instrumentation)
        self.assertIn('root.draw(new Canvas(screenshot))',instrumentation)
        self.assertIn('Bitmap.createBitmap(',instrumentation)
        self.assertIn('api-level: 34',workflow)
        self.assertIn('reactivecircus/android-emulator-runner@v2',workflow)
        self.assertIn('font_scale 1.6',workflow)
        self.assertIn('connectedDebugAndroidTest',workflow)
        self.assertIn('emulator-evidence/recorder_160pct.png',workflow)

if __name__=="__main__": unittest.main()
