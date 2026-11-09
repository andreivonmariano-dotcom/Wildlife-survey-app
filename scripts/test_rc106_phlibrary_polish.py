"""RC106 PHLibrary button audit and concise source-faithful screen contracts."""
import unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/org/phwildlife/survey'
def read(name):return (JAVA/name).read_text(encoding='utf-8')

class Rc106UiContracts(unittest.TestCase):
    def test_four_concise_home_destinations_no_redundant_groups(self):
        s=read('LikasLibraryActivity.java')
        for name in ('Species Atlas','Habitat Atlas','References','Saved species'):
            self.assertIn('tile(body,"'+name+'"',s)
        self.assertEqual(s.count('tile(body,"'),4)
        for old in ('Find a species','"Explore"','"Wildlife groups"',
                    'Discovery Rarity","', 'pickForests'):
            self.assertNotIn(old,s)
        self.assertIn('PhLibraryPhotoHero.create',s)
        hero=read('PhLibraryPhotoHero.java')
        self.assertIn('getBoolean("reduced_motion",false)',hero)
        self.assertIn('ValueAnimator.areAnimatorsEnabled()',hero)
        self.assertIn('onWindowVisibilityChanged',hero)
        self.assertIn('frame.setLayoutParams(new LinearLayout.LayoutParams(-1,coverHeight))',hero)

    def test_explorer_has_one_title_and_one_reset_path(self):
        s=read('PhLibraryDirectoryActivity.java')
        self.assertIn('new ListView(this)',s)
        self.assertIn('setSelectionFromTop',s)
        self.assertIn('resetButton.setVisibility(',s)
        self.assertIn('confirmClearSaved()',s)
        self.assertNotIn('"Clear all filters"',s)
        self.assertNotIn('TextView heading=text("Species atlas"',s)
        self.assertIn('chooseString("Family",true)',s)
        self.assertIn('chooseString("Genus",false)',s)
        self.assertIn('PhLibraryHabitatFilter.mentions(r,selectedHabitat)',s)
        self.assertIn('"Habitat: "',s)
        self.assertIn('selectedSort=0',s)
        self.assertIn('family="";genus="";selectedHabitat=0;',s)

    def test_reader_buttons_keep_original_paragraph_and_back_history(self):
        s=read('PhLibraryBook01.java')
        taxon=read('TaxonDetailActivity.java')
        self.assertIn('record.optString("p"),17.5f',s)
        self.assertIn('PhLibraryStatusBadge.make(a,tier,true)',s)
        self.assertIn('PhLibraryStatusBadge.make(a,global,false)',s)
        self.assertIn('"☆  Save"',s)
        self.assertIn('"✓  Saved"',s)
        self.assertIn('"Citations ("',s)
        self.assertIn('"Same family · "',s)
        self.assertIn('"phlibrary_bib_citation",shortRef',s)
        self.assertNotIn('"Same-family links are taxonomic',s)
        self.assertIn('private void backWithinBook()',taxon)
        self.assertIn('phLibraryHistory.removeLast()',taxon)
        self.assertIn('out.putStringArrayList("phlibrary_history"',taxon)

    def test_references_show_unverified_candidates_not_invented_links(self):
        s=read('PhLibraryBibliographyActivity.java')
        self.assertIn('getStringExtra("phlibrary_bib_citation")',s)
        self.assertIn('candidateMode',s)
        self.assertIn('Not verified',s)
        self.assertIn('this::showIndex',s)
        self.assertIn('setEllipsize(TextUtils.TruncateAt.END)',s)
        self.assertIn('setTextIsSelectable(true)',s)
        self.assertNotIn('not yet completely reconciled',s)
        self.assertNotIn('BOOK 01  /  ORIGINAL BIBLIOGRAPHY',s)

    def test_habitat_evidence_follows_original_source_and_short_labels(self):
        s=read('HabitatSpeciesEvidenceActivity.java')
        self.assertIn('phlibrary_habitat_reader_v1.json',s)
        self.assertNotIn('Literature-linked species',s)
        self.assertIn('Browse species',s)
        self.assertIn('article.setTextIsSelectable(true)',s)
        self.assertIn('articles.optString(id)',s)
        self.assertIn('PhLibraryHabitatFilter.indexOfId',s)
        self.assertIn('Open source',s)
        self.assertNotIn('POSSIBLE FIELD MEASUREMENTS',s)
        self.assertNotIn('getWritableDatabase(',s)
        self.assertNotIn('INSERT INTO',s)

    def test_identity_and_locked_data_invariants(self):
        gradle=(ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 166',gradle)
        self.assertIn("0.27.3-rc106-phlibrary-polish",gradle)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",gradle)
        self.assertIn('super(context,NAME,null,29)',read('FieldDb.java'))
        self.assertIn('EXPECTED_COUNT=715',read('PhLibraryBook01.java'))

if __name__=='__main__':unittest.main()
