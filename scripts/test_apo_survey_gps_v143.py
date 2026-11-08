"""Code143 GPS preflight UX and original GNSS provenance regression."""
import unittest
from pathlib import Path
R=Path(__file__).resolve().parents[1]
def read(p):return (R/p).read_text(encoding="utf-8")
class GpsRC83(unittest.TestCase):
    def test_fix_quality_glance_and_revealed_diagnostics(self):
        s=read("app/src/main/java/org/phwildlife/survey/SurveyGpsActivity.java")
        for token in ('qualitySummary=UiKit.caption(this',
                      '"Required: device GPS accuracy under 10 m, age at most 10 s"',
                      'detailPanel.setVisibility(View.GONE)',
                      '"GPS details","quiet"',
                      'helpPanel.setVisibility(View.GONE)',
                      '"GPS troubleshooting","quiet"',
                      'UiKit.button(this,"Retry GPS acquisition","secondary")',
                      'UiKit.button(this,"Android location settings","secondary")',
                      'Ellipsoidal altitude (device; not terrain elevation): ',
                      'current.hasVerticalAccuracy()','"Mock device position rejected"'):
            self.assertIn(token,s)
        self.assertIn('setQuality("Accuracy "+accuracy+" · Age "+',s)
        self.assertIn('Enable precise location for APO',s)
        self.assertNotIn('Enable Precise location for LIKAS',s)
    def test_survey_and_station_flagged_choices_not_silent(self):
        s=read("app/src/main/java/org/phwildlife/survey/SurveyGpsActivity.java")
        for t in ('UiKit.button(this,"Continue without GPS","warning")',
                  'UiKit.button(this,"Start station without GPS","warning")',
                  'page.addView(footer)', 'setContentView(page)',
                  '"Continue, flag missing GPS"',
                  'if(launchCommitted||skipConfirmationOpen||!acquisitionVisible||isFinishing())return;',
                  'launch.removeExtra("start_gps_latitude")',
                  'launch.removeExtra("start_gps_longitude")',
                  'launch.putExtra("start_without_gps",true)',
                  'result.putExtra("station_gps_missing",fix==null)',
                  'if(!validFix(fix))','GpsFixPolicy.acceptableElapsed(',
                  'launch.putExtra("start_gps_elapsed_nanos",fix.getElapsedRealtimeNanos())'):
            self.assertIn(t,s)
        self.assertLess(s.index("checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)",s.index("private void updateLocation()")),
                        s.index("TrackService.currentFix()",s.index("private void updateLocation()")))
    def test_signed_preview_verification_configured(self):
        wf=read(".github/workflows/apo-survey-gps-v143-emulator-qa.yml")
        for t in ('versionCode=\'143\'','versionName=\'0.27.3-rc83-apo-survey-gps\'',
                  'GpsCompactUiEmulatorTest','Rc55GpsPreflightInstrumentedTest',
                  'font_scale 1.6','font_scale 2.0','wm size 960x2160',
                  'apo_gps_','gps_${mode}_160.png','gps_${mode}_200.png',
                  'len(files)==4'):
            self.assertIn(t,wf)
        gradle=read("app/build.gradle")
        self.assertIn("versionCode 161",gradle)
        self.assertIn("0.27.3-rc101-phlibrary-book-v182",gradle)
        self.assertIn("org.phwildlife.survey.beta08",gradle)
        self.assertIn('super(context,NAME,null,29)',read("app/src/main/java/org/phwildlife/survey/FieldDb.java"))
if __name__=="__main__":unittest.main()
