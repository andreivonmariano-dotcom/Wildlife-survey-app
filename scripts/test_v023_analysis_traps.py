"""Guard linked call analysis and three structured device-survey protocols."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
CODE=ROOT/"app/src/main/java/org/phwildlife/survey"
def read(name):return (CODE/name).read_text(encoding="utf-8")
class V023Audit(unittest.TestCase):
    def test_v24_additive_migration(self):
        db=read("FieldDb.java")
        self.assertIn("super(context,NAME,null,25)",db)
        self.assertIn("if(oldVersion<24)upgradeTwentyFour(d)",db)
        for name in ("acoustic_labels ADD COLUMN taxon_key",
                     "acoustic_labels ADD COLUMN observation_id",
                     "acoustic_labels ADD COLUMN selected_frequency_hz",
                     "CREATE TABLE IF NOT EXISTS trap_deployments(",
                     "CREATE TABLE IF NOT EXISTS trap_checks(",
                     "CREATE TABLE IF NOT EXISTS trap_captures("):
            self.assertIn(name,db)
        self.assertNotIn("DROP TABLE trap_",db)
    def test_spectrogram_controls(self):
        view=read("CallSpectrogramView.java")
        ui=read("CallLibraryActivity.java")
        for tag in ("readWav(File file,long startMs,long endMs)",
                    "setContrast(float factor)","frequencyListener.measured",
                    "AcousticViewport.timeAt"):
            self.assertIn(tag,view)
        for tag in ("+ Zoom","− Zoom","‹ Earlier","Later ›","Spectrogram contrast",
                    "selected_frequency_hz","loadSpectrum(file,clip)"):
            self.assertIn(tag,ui)
    def test_manual_checklist_and_observation_links(self):
        ui=read("CallLibraryActivity.java")
        for tag in ("Choose checklist taxon","Link saved observation",
                    "FROM taxon_catalog WHERE taxon_group=?",
                    "field_records WHERE event_id=? AND taxon_group=?",
                    "CallLinkPolicy.sameSurveyAndGroup","linked_unverified"):
            self.assertIn(tag,ui)
        x=read("AcousticEvidence.java")
        for tag in ("taxon_key","observation_id","selected_frequency_hz","review_status"):
            self.assertIn(tag,x)
    def test_trap_protocol_has_deployment_check_capture_retrieval(self):
        ui=read("TrapProtocolActivity.java")
        for tag in ("Register device deployment","Record check","Retrieve / close device",
                    "Capture and release record","trap_deployments","trap_checks",
                    "trap_captures","validatedUtc"):
            self.assertIn(tag,ui)
        main=read("MainActivity.java")
        self.assertIn("TrapProtocolPolicy.canComplete",main)
        self.assertIn("TrapEvidence.writeZip(",main)
        self.assertIn("TrapProtocolActivity.class",main)
        self.assertIn("TrapProtocolActivity.class",read("HistoryActivity.java"))
        self.assertIn("TrapProtocolActivity.class",read("WelcomeActivity.java"))
    def test_outputs_in_zipped_research_export(self):
        x=read("TrapEvidence.java")
        for tag in ("trap_deployments.csv","trap_checks.csv","trap_captures.csv"):
            self.assertIn(tag,x)
    def test_methods_are_structured(self):
        x=read("SurveyMethods.java")
        for method in ("camera_trap_check","live_trap_check","drift_fence_check"):
            self.assertIn('new Method("'+method,x)
        self.assertIn('new Method("bat_acoustic"',x)
if __name__=="__main__":unittest.main()
