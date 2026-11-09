"""RC101 canonical PHLibrary Book v1.82 asset regression, no Android SDK required."""
import base64
from collections import Counter
import gzip
import hashlib
import json
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'app/src/main/assets'
JAVA=ROOT/'app/src/main/java/org/phwildlife/survey'
BOOK_SHA='a87021a615a93f64b56cb3c8974e57deaf69bdd7f0ca7478b70068bad533e885'
BIB_SHA='cb597385f1fc10bcca5f3f9aa5ddafd64b37d762f481816ca479e26df3a9751e'


def read_pack(name,sha):
    data=gzip.decompress(base64.b64decode((ASSETS/name).read_text(encoding='ascii')))
    assert hashlib.sha256(data).hexdigest()==sha,name
    return json.loads(data)


class CanonicalBook01V182(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.book=read_pack('phlibrary_book01_v182.json.gz.b64',BOOK_SHA)
        cls.refs=read_pack('phlibrary_book01_bibliography_v182.json.gz.b64',BIB_SHA)
        cls.crosswalk=gzip.decompress(base64.b64decode((ASSETS/'phlibrary_book01_taxon_crosswalk_v182.json.gz.b64').read_text(encoding='ascii')))

    def test_exact_content_counts_and_one_paragraph(self):
        book=self.book;records=book['records']
        self.assertEqual((book['edition'],book['rarityModel'],book['speciesCount']),('1.82','0.3.4',715))
        self.assertEqual(len(records),715)
        self.assertEqual(Counter(r['g'] for r in records),{'Birds':409,'Amphibians':53,'Reptiles':154,'Mammals':99})
        self.assertEqual(Counter(r['t'] for r in records),{'Common':124,'Uncommon':336,'Rare':214,'Exceptionally rare':37,'Legendary':3,'Unranked':1})
        self.assertEqual(len(set(r['s'].casefold() for r in records)),715)
        for r in records:
            with self.subTest(taxon=r['s']):
                self.assertTrue(all(r[k] for k in ('s','c','g','o','f','b','p')))
                self.assertNotIn('\n',r['p'])
                self.assertNotIn('associated with',r['p'].lower())
                self.assertTrue(r['r'])
                for ref in r['r']:
                    self.assertIn(ref['short'],r['p'])
                    self.assertEqual(ref['resolution'],'unresolved')

    def test_splits_and_legacy_identifications(self):
        names={x['s']:x for x in self.book['records']}
        self.assertNotIn('Aphantophryne nana',names)
        self.assertEqual(names['Oreophryne cf. nana']['t'],'Unranked')
        self.assertIsNone(names['Oreophryne cf. nana']['iucn'])
        self.assertEqual(names['Ceyx melanurus']['t'],'Rare')
        self.assertEqual(names['Ceyx mindanensis']['t'],'Uncommon')
        x=json.loads(self.crosswalk)
        self.assertEqual(x['edition'],'1.82')
        self.assertEqual(len(x['crosswalks']),2)
        self.assertTrue(all(z['autoMapObservations'] is False for z in x['crosswalks']))

    def test_original_bibliography_separate_and_source_preserved(self):
        self.assertEqual(self.refs['edition'],'1.82')
        self.assertEqual(len(self.refs['bibliography']),1159)
        for ref in self.refs['bibliography']:
            self.assertTrue(ref['text'])
            if ref['url']:self.assertTrue(ref['url'].startswith('http'))

    def test_atomic_reader_and_no_survey_schema_modification(self):
        source=(JAVA/'PhLibraryBook01.java').read_text(encoding='utf-8')
        self.assertIn('EXPECTED_COUNT=715',source)
        self.assertIn(BOOK_SHA,source)
        self.assertIn(BIB_SHA,source)
        self.assertIn('verify(root,source)',source)
        self.assertIn('Base64InputStream',source)
        self.assertIn('GZIPInputStream',source)
        for method in ('applyEditorial(', 'applyReaderRefinements(', 'applyHabitatCues(', 'applyReaderPolish(', 'applyHabitatIntegration('):
            self.assertNotIn(method,source)
        self.assertNotIn('getWritableDatabase',source)
        self.assertNotIn('execSQL(',source)
        self.assertIn('super(context,NAME,null,29)',(JAVA/'FieldDb.java').read_text())
        self.assertIn('versionCode 165',(ROOT/'app/build.gradle').read_text())
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",(ROOT/'app/build.gradle').read_text())
        self.assertIn('.PhLibraryBibliographyActivity',(ROOT/'app/src/main/AndroidManifest.xml').read_text())

    def test_real_account_search_and_unranked_filter(self):
        directory=(JAVA/'PhLibraryDirectoryActivity.java').read_text(encoding='utf-8')
        self.assertIn('r.optString("p")',directory)
        self.assertIn('"Legendary","Unranked"',directory)
        self.assertIn('matches.size()',directory)
        self.assertIn('PhLibraryBook01.ready(this)',directory)
        self.assertIn('"phlibrary_scientific_name"',directory)

if __name__=='__main__':unittest.main()