"""RC88: detection-first field recorder, no synthetic observations."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]

def read(name):
    return (ROOT/"app/src/main/java/org/phwildlife/survey"/name).read_text()

class DetectionFirstContracts(unittest.TestCase):
    def test_mark_is_before_species_and_optional_tools(self):
        source=read("MainActivity.java")
        self.assertLess(source.index('content.addView(directActions);'),
                        source.index('taxonEntry=new AutoCompleteTextView(this);'))
        self.assertLess(source.index('content.addView(directActions);'),
                        source.index('LinearLayout birdQuick=vertical();'))
        self.assertIn('recorderTools.removeView(pointNextButton);',source)
        self.assertIn('content.addView(pointNextButton,stationLp);',source)
        self.assertIn('utilityRow.removeView(pauseButton);',source)
        self.assertIn('content.addView(markRowView',source)
    def test_mark_is_a_durable_draft_and_never_a_record(self):
        source=read("MainActivity.java")
        block=source[source.index('private void captureDetectionMark()'):
                     source.index('private void openFieldProtocol()',source.index('private void captureDetectionMark()'))]
        self.assertIn('markedBearing=null;markedPitch=null;sensorQuality=-1;',block)
        self.assertIn('markedAt=System.currentTimeMillis();',block)
        self.assertIn('markedObserverFix=freshObserverFix();',block)
        self.assertIn('persistDraft(true)',block)
        self.assertNotIn('INSERT INTO field_records',block)
        self.assertIn('if(immediate)return editor.commit();',source)
        self.assertIn('d.put("markedTime",markedAt);',source)
        self.assertIn('d.put("markedGpsAccuracy"',source)
        self.assertIn('long first=markedAt>0?markedAt:System.currentTimeMillis();',source)
    def test_paused_or_unstarted_station_cannot_mark(self):
        source=read("MainActivity.java")
        method=source[source.index('private void markDetection()'):
                      source.index('private void captureDetectionMark()')]
        for phrase in ('if(surveyPaused)','method.equals("point_count")&&pointCountId==null',
                       'requiresGenericUnit()&&samplingUnitId==null',
                       'if(markedAt>0)','Replace detection time?',
                       'Keep earlier mark'):
            self.assertIn(phrase,method)
    def test_survey_outcomes_and_database_unchanged(self):
        main=read("MainActivity.java")
        db=read("FieldDb.java")
        self.assertIn('super(context,NAME,null,29)',db)
        self.assertIn('UiKit.button(this,"End Survey","outline-danger")',main)
        self.assertIn('if(hasUnsavedObservation())',main)
        self.assertIn('markedAt>0||count!=1',main)
        self.assertIn("versionCode 161",(ROOT/"app/build.gradle").read_text())
        self.assertIn('0.27.3-rc101-phlibrary-book-v182',(ROOT/"app/build.gradle").read_text())

if __name__=="__main__":
    unittest.main()
