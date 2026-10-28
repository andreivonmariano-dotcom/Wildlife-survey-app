"""v0.26 source contracts for the field UI declutter and research safeguards."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(filename):
    return (CODE/filename).read_text(encoding="utf-8")

class CleanFieldUi(unittest.TestCase):
    def test_recorder_has_only_one_browse_action_and_persistent_end(self):
        screen=src("MainActivity.java")
        self.assertIn('UiKit.button(this,"Browse","secondary")',screen)
        self.assertIn('taxonEntry.setHint("Species or field ID")',screen)
        self.assertNotIn('UiKit.button(this,"Find species","primary")',screen)
        self.assertNotIn('content.addView(section("Species"))',screen)
        self.assertIn('UiKit.button(this,"Save Observation","primary")',screen)
        self.assertIn('UiKit.button(this,"End Survey","outline-danger")',screen)
        self.assertIn('SurveyClosePolicy.resolve(',screen)
    def test_recorder_still_offers_original_evidence_and_protocol_actions(self):
        screen=src("MainActivity.java")
        for key in ('openCallLibrary()','openTrapProtocol()','openFieldProtocol()',
                    'markDetection()','openCamera()','pickPhoto()','openSpeciesPicker()'):
            self.assertIn(key,screen)
        self.assertIn('pauseStatusView.setVisibility(',screen)
        self.assertIn('progressView.setVisibility(',screen)
    def test_home_puts_shortcuts_first_and_groups_other_tools(self):
        home=src("WelcomeActivity.java")
        for key in ('"Survey history"','"Search Species Library"',
                    '"Field tools & settings  ▾"','"Field evidence"',
                    '"Workspace"','"Species Library"',
                    '"Study and archive management"'):
            self.assertIn(key,home)
        self.assertNotIn('"History & tools  ▾"',home)
        self.assertIn('toolRow(tools,"Import previous backup"',home)
    def test_study_template_is_never_silently_hidden_after_selection(self):
        home=src("WelcomeActivity.java")
        for key in ('refreshStudyTitle()', 'selectedStudyId()',
                    '"studyTemplateId"', '"＋ Study: "',
                    'String savedStudy=p.getString("studyTemplateId","")'):
            self.assertIn(key,home)
        self.assertIn('i.putExtra("study_protocol_id"',home)
    def test_history_shows_survey_relevant_actions(self):
        history=src("HistoryActivity.java")
        for key in ('TrapProtocolPolicy.supported(surveyMethod)',
                    'FieldProtocolPolicy.supported(surveyMethod)',
                    'if(birdOrHerp)', 'if(trap||field)',
                    'searchHandler.postDelayed(pendingSearch,220L)',
                    'searchHandler.removeCallbacksAndMessages(null)'):
            self.assertIn(key,history)
        self.assertNotIn('UiKit.button(this,"Camera / trap deployment history"',history)
    def test_closed_legacy_records_keep_correct_outcome(self):
        history=src("HistoryActivity.java")
        for key in ("OUTCOME_SQL","WHEN s.ended IS NULL THEN 'active'",
                    "WHEN COALESCE(s.complete,0)=1 THEN 'complete'",
                    "SurveyMethods.displayLabel"):
            self.assertIn(key,history)
    def test_missing_original_photos_cannot_be_ignored_in_exports(self):
        photos=src("PhotoEvidence.java")
        self.assertIn('throw new FileNotFoundException(',photos)
        self.assertNotIn('if(!f.isFile())continue;',photos)
        self.assertIn('zip.putNextEntry(new ZipEntry("photos/"+name))',photos)
    def test_species_context_not_repeated_as_permanent_header(self):
        picker=src("SpeciesPickerActivity.java")
        self.assertIn('updateSiteSummary();',picker)
        self.assertNotIn('root.addView(siteSummary);',picker)
        self.assertIn('TextView geographic=text("Geographic source: "+',picker)
        self.assertIn('item.screening.habitatStatus',picker)
        self.assertIn('item.screening.elevationStatus',picker)
        self.assertNotIn('item.screening.scorePercent+"% screening index',picker)

    def test_method_capture_tools_are_disclosed_without_hiding_recording_actions(self):
        recorder=src("MainActivity.java")
        self.assertIn('evidenceTools.setVisibility(View.GONE)',recorder)
        self.assertIn('evidenceTools.addView(sound,soundLp)',recorder)
        self.assertIn('evidenceTools.addView(devices,deviceLp)',recorder)
        self.assertIn('evidenceTools.addView(methodRecords,methodLp)',recorder)
        self.assertIn('content.addView(FieldVisuals.section(this,"Record observation"',recorder)
        self.assertIn('UiKit.button(this,"Save Observation","primary")',recorder)
        self.assertIn('UiKit.button(this,"End Survey","outline-danger")',recorder)

    def test_species_library_summaries_are_source_driven(self):
        catalog=src("CatalogActivity.java")
        self.assertIn('UiKit.title(this,"Species Library")',catalog)
        self.assertIn('append(attributedTotal)',catalog)
        self.assertNotIn('1503 catalog entries',catalog)
        self.assertNotIn('result.toString().replace(',catalog)
        detail=src("TaxonDetailActivity.java")
        self.assertIn('Diagnostic identification traits',detail)
        self.assertIn('A checked diagnostic comparison is not yet available',detail)
        self.assertIn('Regional source evidence',detail)
        self.assertIn('Compare source-checked ID characters',detail)
        self.assertIn('DiagnosticGuideActivity.hasProfile',detail)
        self.assertIn('FieldQuickActions.forSurvey(profile,method)',src("MainActivity.java"))
        self.assertIn('FieldVisuals.section(this,"Fieldwork"',src("WelcomeActivity.java"))

if __name__=="__main__":
    unittest.main()
