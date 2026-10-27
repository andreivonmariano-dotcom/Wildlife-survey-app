"""Release candidate GPS audit must never block callbacks with a SQLite transaction."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class GpsWriterTests(unittest.TestCase):
    def test_ordered_background_writer_and_shutdown(self):
        src=(JAVA/"TrackService.java").read_text(encoding="utf-8")
        self.assertIn("trackWriter=Executors.newSingleThreadExecutor()",src)
        start=src.index("private void record(Location l,double step")
        end=src.index("private void stopRecording()",start)
        region=src[start:end]
        self.assertIn("trackWriter.execute(()->{",region)
        self.assertIn("d.beginTransaction()",region)
        self.assertIn("gps_track_m=?",region)
        self.assertIn("new Object[]{survey,time,lat,lon,accuracy,step,accepted?1:0,reason}",region)
        shutdown=src[src.index("@Override public void onDestroy()"):]
        self.assertLess(shutdown.index("trackWriter.execute("),
                        shutdown.index("trackWriter.shutdown()"))
        self.assertIn("if(db!=null)db.close()",shutdown)

if __name__=="__main__":
    unittest.main()
