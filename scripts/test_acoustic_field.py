"""CI source contracts for long-lived acoustic evidence and field form layouts."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

def source(name):
    return (JAVA/name).read_text(encoding="utf-8")

class AcousticFieldTest(unittest.TestCase):
    def test_method_is_not_basic_log(self):
        m=source("SurveyMethods.java")
        self.assertIn('"amphibian_calls","Amphibian calling"',m)
        self.assertIn("spectrogram and time-coded labels.",m)
        self.assertIn('new Method("coverboard_search"',m)

    def test_private_wav_and_persistent_labels_schema(self):
        db=source("FieldDb.java")
        self.assertIn("super(context,NAME,null,24)",db)
        self.assertIn("if(oldVersion<23)upgradeTwentyThree(d)",db)
        self.assertIn("CREATE TABLE IF NOT EXISTS acoustic_clips(",db)
        self.assertIn("CREATE TABLE IF NOT EXISTS acoustic_labels(",db)
        self.assertNotIn("DROP TABLE acoustic_clips",db)

    def test_live_and_archived_spectrogram(self):
        spectro=source("CallSpectrogramView.java")
        self.assertIn("SpectrogramMath.analyze",spectro)
        self.assertIn("float[][] readWav(File file)",spectro)
        self.assertIn("setCursor",spectro)
        audio=source("CallAudioRecorder.java")
        self.assertIn("SAMPLE_RATE=44100",audio)
        self.assertIn("MAX_SECONDS=600",audio)
        self.assertIn("AudioRecord",audio)
        self.assertIn("writeHeader(wav,frames*2)",audio)

    def test_labels_and_recordings_available_after_survey(self):
        archive=source("CallLibraryActivity.java")
        self.assertIn("FROM acoustic_clips c LEFT JOIN surveys",archive)
        self.assertIn('FROM acoustic_labels WHERE clip_id=?',archive)
        self.assertIn("player.setDataSource(file.getAbsolutePath())",archive)
        self.assertIn("newLabel(String clip)",archive)
        self.assertIn("canRecord()",archive)
        history=source("HistoryActivity.java")
        home=source("WelcomeActivity.java")
        self.assertIn("CallLibraryActivity.class",history)
        self.assertIn("CallLibraryActivity.class",home)

    def test_wav_exports_with_provenance(self):
        output=source("AcousticEvidence.java")
        self.assertIn('"acoustic_clips.csv"',output)
        self.assertIn('"acoustic_labels.csv"',output)
        self.assertIn('new ZipEntry("audio/"+name)',output)
        self.assertIn("if(!source.isFile())throw new FileNotFoundException",output)
        self.assertIn("AcousticEvidence.writeZip(",source("MainActivity.java"))

    def test_taxon_quick_fields_not_lost(self):
        main=source("MainActivity.java")
        for field in ("distanceSourceSpinner","firstPositionSpinner","movementSpinner",
                      "callScoreSpinner","noiseSpinner","evidenceSpinner","deviceEntry"):
            self.assertIn(field,main)
        self.assertIn('content.addView(birdQuick)',main)
        self.assertIn('content.addView(herpQuick)',main)
        self.assertIn('content.addView(mammalQuick)',main)

    def test_manifest(self):
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
        self.assertIn("android.permission.RECORD_AUDIO",manifest)
        self.assertIn(".CallLibraryActivity",manifest)
        self.assertIn('android:exported="false"',manifest)

if __name__=="__main__":
    unittest.main()
