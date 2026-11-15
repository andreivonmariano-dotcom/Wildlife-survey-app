"""Source contracts for private offline locality filters and saved research citations."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
def source(name):return (JAVA/name).read_text(encoding="utf-8")

class LocalitiesResearchContracts(unittest.TestCase):
    def test_offline_map_filters_original_coordinates_without_writes(self):
        s=source("ApoOfflineMapActivity.java")
        for token in ('SQLiteDatabase.OPEN_READONLY','COALESCE(project_id,\'default\')',
                      '"SELECT id,name FROM projects', '"survey".equals(pin.kind)',
                      'private String kindFilter="all"', 'private String projectFilter=""',
                      'kindFilter.equals(p.kind)', 'projectFilter.equals(p.projectId)',
                      'showLocalities&&inside(', 'getPreferences(MODE_PRIVATE)',
                      'chooseProject()', 'chooseFilters()',
                      'new Intent(this,HistoryActivity.class)',
                      '.putExtra("event_id",pin.recordId)',
                      'new Intent(this,SitesActivity.class)'):
            self.assertIn(token,s)
        for forbidden in ('getWritableDatabase(', 'INSERT INTO', 'UPDATE surveys',
                          'DELETE FROM', 'HttpURLConnection', 'new URL('):
            self.assertNotIn(forbidden,s)

    def test_saved_bibliography_does_not_mutate_reference_assets(self):
        s=source("PhLibraryBibliographyActivity.java")
        bookmarks=source("ApoReferenceBookmarks.java")
        self.assertIn('PhLibraryBook01.bibliography(this)',s)
        self.assertIn('ApoReferenceBookmarks.isSaved(this,citation)',s)
        self.assertIn('ApoReferenceBookmarks.setSaved(this,full,!isSaved)',s)
        self.assertIn('savedOnly?"All references"',s)
        self.assertIn('out.putBoolean("saved_only",savedOnly)',s)
        self.assertIn('Full text: unavailable in this offline edition.',s)
        self.assertIn('Online source: connection required.',s)
        self.assertIn('sourceUrl.startsWith("https://")',s)
        self.assertIn('MessageDigest.getInstance("SHA-256")',bookmarks)
        self.assertIn('new HashSet<>(prefs.getStringSet(',bookmarks)
        self.assertNotIn('getWritableDatabase',s+bookmarks)
        self.assertNotIn('ACTION_OPEN_DOCUMENT',s+bookmarks)

    def test_android_identity_and_survey_outcomes_stable(self):
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn('super(context,NAME,null,29)',source('FieldDb.java'))
        recorder=source('MainActivity.java')
        for outcome in ('"End Survey"','"Save Observation"',
                        '"Cancel Survey · Not Counted"','SurveyClosePolicy.resolve('):
            self.assertIn(outcome,recorder)

if __name__=="__main__":
    unittest.main()
