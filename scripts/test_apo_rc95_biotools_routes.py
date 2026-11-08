"""RC95 survey navigation contract. Checks actual Android destination wiring, not labels alone."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"app/src/main/java/org/phwildlife/survey"

def read(name):
    return (SRC/name).read_text(encoding="utf-8")

class SurveyNavigationRC95(unittest.TestCase):
    def test_workflow_from_setup_through_end_preserves_scientific_states(self):
        hub=read("WelcomeActivity.java")
        gps=read("SurveyGpsActivity.java")
        guide=read("HabitatsSpeciesActivity.java")
        rec=read("MainActivity.java")
        self.assertIn('UiKit.navigationHeader(this,"Surveys","BioTools"',hub)
        self.assertIn('setupStartButton.setText("Continue to GPS")',hub)
        self.assertIn("this::fieldworkHub",hub)
        self.assertIn('setPositiveButton("Continue, flag missing GPS"',gps)
        self.assertIn('"Survey setup",this::finish,body',guide)
        self.assertIn('UiKit.button(this,"End Survey"',rec)
        for label in ['"Finish Complete"','"End Incomplete"',
                      '"Cancel Survey · Not Counted"']:
            self.assertIn(label,rec)
        self.assertIn("SurveyClosePolicy.resolve(",rec)

    def test_live_review_returns_to_recorder_not_history_root(self):
        rec=read("MainActivity.java")
        history=read("HistoryActivity.java")
        self.assertIn('new Intent(this,HistoryActivity.class)',rec)
        self.assertIn('.putExtra("navigation_origin","recorder")',rec)
        self.assertIn('private boolean fromRecorder()',history)
        self.assertIn('if(eventFilter==null||fromRecorder())',history)
        self.assertIn('this::returnToCaller',history)
        self.assertIn('onBackPressed(){returnToCaller();}',history)
        self.assertIn('"Live survey":"All surveys"',history)
        self.assertEqual(history.count('.putExtra("navigation_origin","history")'),3)

    def test_recorder_back_and_home_require_same_confirmation(self):
        s=read("MainActivity.java")
        self.assertIn('home.setOnClickListener(v->requestLeaveRecorder())',s)
        self.assertIn('onBackPressed(){requestLeaveRecorder();}',s)
        self.assertIn('"Leave survey running?"',s)
        self.assertIn('"Keep recording"',s)
        self.assertIn('"Go Home"',s)
        self.assertIn('persistDraft();\n        returnToHome=true;',s)

    def test_protocol_and_trap_back_are_caller_aware(self):
        for filename in ["FieldProtocolsActivity.java","TrapProtocolActivity.java"]:
            s=read(filename)
            for token in ('private String returnLabel()',
                          '"recorder".equals(origin)', '"history".equals(origin)',
                          'this::returnToCaller','onBackPressed(){returnToCaller();}',
                          'getIntent().getStringExtra("event_id")==null'):
                self.assertIn(token,s)
        trap=read("TrapProtocolActivity.java")
        self.assertIn("FROM surveys s WHERE s.method IN",trap)
        self.assertIn("'camera_trap_check','drift_fence_check','live_trap_check'",trap)
        self.assertNotIn("FROM trap_deployments d JOIN surveys s",trap)
        self.assertNotIn("method=cMethod(id)",trap)
        self.assertIn('method=selectedMethod;draw()',trap)

    def test_station_progress_only_counts_this_event_visits(self):
        s=read("StationProgressActivity.java")
        self.assertIn('" WHERE event_id=? ORDER BY sequence_number"',s)
        self.assertIn('"No visits recorded yet.',s)
        self.assertIn('"history".equals(origin)?"Observations"',s)
        self.assertNotIn("FROM sites WHERE project_id=?",s)
        self.assertNotIn("Project stations without a recorded visit",s)

    def test_audio_header_and_android_back_share_save_aware_action(self):
        s=read("CallLibraryActivity.java")
        self.assertIn('this::navigateBack',s)
        self.assertIn('onBackPressed(){navigateBack();}',s)
        self.assertIn('if(recorder.isCapturing())stopRecord();',s)
        self.assertIn('else notice("Finish saving this recording before leaving.")',s)
        self.assertIn('if(selectedClip!=null)',s)

    def test_utility_return_labels_and_field_records_tab(self):
        hub=read("WelcomeActivity.java")
        self.assertIn('new Intent(this,HabitatRecordsActivity.class)\n                    .putExtra("initial_tab","records")',hub)
        for f in ["SitesActivity.java","StudyProtocolsActivity.java",
                  "HabitatRecordsActivity.java"]:
            self.assertIn('"Previous screen",this::finish',read(f))
        self.assertIn('"Back to previous screen":"Back to "+backLabel',read("UiKit.java"))
        self.assertIn('"Survey setup",this::finish,body',read("HabitatsSpeciesActivity.java"))

    def test_identity_and_database_unchanged(self):
        build=(ROOT/"app/build.gradle").read_text()
        self.assertIn("versionCode 160",build)
        self.assertIn("0.27.3-rc100-integrated-habitats",build)
        self.assertIn("org.phwildlife.survey.beta08",build)
        self.assertIn("super(context,NAME,null,29)",read("FieldDb.java"))
        self.assertIn("FieldBackupArchive",read("FieldBackupActivity.java"))

if __name__=="__main__":
    unittest.main()
