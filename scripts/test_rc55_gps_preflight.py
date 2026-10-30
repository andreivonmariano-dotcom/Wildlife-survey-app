"""RC55 screen 4 GPS preflight UI and scientific provenance regression."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def src(n):return (JAVA/n).read_text(encoding="utf-8")
class Rc55GpsUiContract(unittest.TestCase):
    def test_common_page_header_and_persistent_safe_actions(self):
        gps=src("SurveyGpsActivity.java")
        self.assertIn('UiKit.page(this,stationMode?"Station GPS":"Survey GPS"',gps)
        self.assertIn('stationMode?"Survey":"Survey setup",this::cancel,content',gps)
        self.assertIn("page.addView(footer)",gps)
        self.assertIn("setContentView(page)",gps)
        self.assertIn('UiKit.button(this,"Check current GPS again","secondary")',gps)
        self.assertIn('UiKit.button(this,"Open location settings","secondary")',gps)
        self.assertIn('UiKit.button(this,"Start without GPS · mark start missing","warning")',gps)
        self.assertIn('UiKit.button(this,"Start without GPS · flag missing","warning")',gps)
        self.assertIn("startButton.setVisibility(View.GONE)",gps)
        self.assertIn("this::cancel",gps)
    def test_missing_fix_and_station_paths_stay_explicit(self):
        gps=src("SurveyGpsActivity.java")
        for token in (
            'startButton.setOnClickListener(v->launchWithFix())',
            'if(launchCommitted||skipConfirmationOpen||!acquisitionVisible||isFinishing())return;',
            'new AlertDialog.Builder(this)',
            'launch.putExtra("start_without_gps",true)',
            'launch.removeExtra("start_gps_latitude")',
            'result.putExtra("station_gps_missing",fix==null)',
            'start_gps_recorded_at_ms',
            'start_gps_elapsed_nanos',
            'finishStation(null)',
        ):self.assertIn(token,gps)
        self.assertIn("validFix(Location fix)",gps)
        self.assertIn("GpsFixPolicy.acceptableElapsed(",gps)
    def test_invalid_states_not_overwritten_and_altitude_not_msl(self):
        gps=src("SurveyGpsActivity.java")
        func=gps.split("private void updateLocation(){",1)[1].split("private void launchWithFix(){",1)[0]
        self.assertLess(func.index("checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)"),
                        func.index("TrackService.currentFix()"))
        self.assertLess(func.index("!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)"),
                        func.index("TrackService.currentFix()"))
        self.assertIn('"Device GPS is switched off"',func)
        self.assertIn('"Precise location permission required"',func)
        self.assertIn('"GPS receiver is not running"',func)
        self.assertIn("Ellipsoidal altitude (device; not terrain elevation): ",func)
        self.assertIn("current.hasVerticalAccuracy()",func)
        self.assertNotIn("mean sea level",func.lower())
        build=(ROOT/"app/build.gradle").read_text(encoding="utf8")
        self.assertIn("versionCode 121",build)
        self.assertIn("0.27.3-rc61-library-icon-preview",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn("super(context,NAME,null,27)",src("FieldDb.java"))
if __name__=="__main__":unittest.main()
