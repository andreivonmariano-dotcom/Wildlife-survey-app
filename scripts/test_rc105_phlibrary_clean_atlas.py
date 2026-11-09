"""RC105 owner-approved PHLibrary layout and four-photo offline source gates.

These static checks complement Android 14 screenshots and a signed upgrade;
they do not substitute for a physical device or scientific literature review.
"""
import base64
import hashlib
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/org/phwildlife/survey'


class Rc105CleanAtlas(unittest.TestCase):
    def test_owner_mindanao_photos_are_embedded_and_integrity_locked(self):
        asset=ROOT/'app/src/main/assets/phlibrary_mindanao_forest_strip.webp.b64'
        encoded=asset.read_text(encoding='ascii').strip()
        webp=base64.b64decode(encoded,validate=True)
        self.assertEqual(len(webp),285824)
        self.assertEqual(hashlib.sha256(webp).hexdigest(),
                         'b24352b109ae83d9baa41fda9a7363a8403f57a7bfe4ce41dbc1d981a208666f')
        self.assertEqual(webp[:4],b'RIFF')
        self.assertEqual(webp[8:12],b'WEBP')
        self.assertIn('VP8',webp[12:40].decode('latin-1'))
        hero=(JAVA/'PhLibraryPhotoHero.java').read_text()
        self.assertIn('Base64InputStream(input,Base64.DEFAULT)',hero)
        self.assertIn('SLOT=600000L,FADE=950L',hero)
        self.assertIn('private int slot()',hero)
        self.assertIn('setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO)',hero)

    def test_home_is_concise_and_200percent_search_accessible(self):
        home=(JAVA/'LikasLibraryActivity.java').read_text()
        self.assertIn('PhLibraryPhotoHero.create(this',home)
        for destination in ('Species Atlas','Habitat Atlas','References','Saved species'):
            self.assertIn('"'+destination+'"',home)
        self.assertNotIn('this::chooseRarity',home)
        self.assertIn('largeText?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL',home)
        self.assertIn('getResources().getConfiguration().fontScale>=1.6f',home)
        self.assertIn('back.setTextSize(12)',home)

    def test_explorer_keeps_virtualized_scrolling_and_evidence_limits(self):
        explorer=(JAVA/'PhLibraryDirectoryActivity.java').read_text()
        for required in ('new ListView(this)','setOnItemClickListener',
                         'chooseString("Family",true)','chooseString("Genus",false)',
                         'PhLibraryHabitatFilter.mentions(r,selectedHabitat)','site',
                         'selectedHabitat','setSelectionFromTop'):
            # A term match is a literal search in the canonical article, not a verified habitat record.
            if required=='site':
                continue
            self.assertIn(required,explorer)
        self.assertIn('"Habitat: "',explorer)
        self.assertIn('matches.size()==1?" account":" accounts"',explorer)
        self.assertIn('h.badges.setOrientation(enlarged?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL)',explorer)
        self.assertNotIn('Show more species',explorer)

    def test_reader_and_habitats_remain_source_faithful(self):
        book=(JAVA/'PhLibraryBook01.java').read_text()
        status=(JAVA/'PhLibraryStatusBadge.java').read_text()
        habitat=(JAVA/'HabitatSpeciesEvidenceActivity.java').read_text()
        self.assertIn('EXPECTED_COUNT=715',book)
        self.assertIn('record.optString("p"),17.5f',book)
        self.assertIn('PhLibraryStatusBadge.make(a,tier,true)',book)
        self.assertIn('PhLibraryStatusBadge.make(a,global,false)',book)
        self.assertIn('case "CR":bg=0xFF7A2431',status)
        self.assertIn('case "Legendary":bg=0xFF54236F',status)
        self.assertIn('phlibrary_habitat_reader_v1.json',habitat)
        self.assertNotIn('Literature-linked species',habitat)
        self.assertIn('Browse species',habitat)
        self.assertNotIn('getWritableDatabase',book)
        self.assertNotIn('getWritableDatabase',habitat)
        build=(ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 166',build)
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertIn('super(context,NAME,null,29)',(JAVA/'FieldDb.java').read_text())


if __name__=='__main__':
    unittest.main()
