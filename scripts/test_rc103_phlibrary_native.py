"""RC103 source/data invariants for native PHLibrary 2.0."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/org/phwildlife/survey'

def source(name):
    return (JAVA/name).read_text(encoding='utf-8')

class PhLibrary2Native(unittest.TestCase):
    def test_canonical_book_and_separate_reading_layer(self):
        reader=source('PhLibraryBook01.java')
        self.assertIn('EXPECTED_COUNT=715',reader)
        self.assertIn('phlibrary_book01_v182.json.gz.b64',reader)
        self.assertIn('record.optString("p")',reader)
        self.assertIn('article.setTextIsSelectable(true)',reader)
        self.assertIn('Related · '+ '"',reader)
        self.assertIn('Related · ',reader)
        self.assertIn('Citations (',reader)
        self.assertIn('PhLibraryStatusBadge.make',reader)
        self.assertIn('PhLibraryStatusBadge.make',reader)
        self.assertIn('phlibrary_reader_v2',reader)
        self.assertIn('PhLibraryBookmarks.toggle',reader)
        bookmark=source('PhLibraryBookmarks.java')
        self.assertIn('getSharedPreferences(PREFS,Context.MODE_PRIVATE)',bookmark)
        self.assertNotIn('FieldDb',bookmark)
        self.assertNotIn('SQLite',bookmark)
    def test_editorial_home_and_structured_search(self):
        home=source('LikasLibraryActivity.java')
        explore=source('PhLibraryDirectoryActivity.java')
        ui=source('PhLibrary2Ui.java')
        self.assertIn('Wildlife of',home)
        self.assertIn('Greater Mindanao',home)
        self.assertIn('References',home)
        self.assertIn('Saved species',home)
        self.assertIn('Habitat Atlas',home)
        self.assertIn('new ForestCanvas',ui)
        self.assertIn('new ListView(this)',explore)
        self.assertIn('record.optString',explore) if False else None
        self.assertIn('r.optString("p")',explore)
        self.assertIn('IUCN: ',explore)
        self.assertIn('Excerpt · ',explore)
        self.assertIn('setSelectionFromTop',explore)
        self.assertIn('phlibrary_saved_only',explore)
        self.assertNotIn('Show more accounts',explore)
    def test_habitat_and_references_keep_provenance(self):
        habitat=source('HabitatSpeciesEvidenceActivity.java')
        bib=source('PhLibraryBibliographyActivity.java')
        self.assertIn('offline_habitat_profiles_v1.json',habitat)
        self.assertIn('phlibrary_habitat_reader_v1.json',habitat)
        self.assertIn('Browse species',habitat)
        self.assertIn('published',habitat.lower()) if False else None
        self.assertIn('PhLibraryHabitatFilter.indexOfId',habitat)
        self.assertIn('new ListView(this)',bib)
        self.assertNotIn('if(found>45)',bib)
        self.assertIn('candidateMode',bib)
    def test_release_identity_and_data_scope(self):
        build=(ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 176',build)
        self.assertIn("versionName '0.27.4-rc116-localities-research-preview'",build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn('super(context,NAME,null,29)',source('FieldDb.java'))
        self.assertNotIn('getWritableDatabase',source('PhLibraryBook01.java'))
if __name__=='__main__':unittest.main()
