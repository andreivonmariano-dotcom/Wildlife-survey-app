"""Offline map and route-consolidation source contracts for APO RC114 continuation."""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

def source(name):return (JAVA/name).read_text(encoding="utf-8")

class OfflineMapContract(unittest.TestCase):
    def test_boundary_data_is_bundled_with_original_provenance(self):
        asset=ROOT/"app/src/main/assets/ph_provinces_2026q3.json"
        self.assertTrue(asset.exists())
        src=source("BoundaryLookup.java")
        self.assertIn('SOURCE="faeldon/philippines-json-maps"',src)
        self.assertIn('VERSION="PSGC-2026-Q3-lowres-v1"',src)
        self.assertIn('static synchronized JSONArray mapFeatures(',src)
        self.assertIn('load(context);',src)

    def test_map_uses_readonly_existing_provenance_and_no_network(self):
        src=source("ApoOfflineMapActivity.java")
        self.assertIn('SQLiteDatabase.OPEN_READONLY',src)
        self.assertIn('FieldDb.NAME',src)
        self.assertIn('FROM sites ',src)
        self.assertIn('start_gps_latitude',src)
        self.assertIn('start_gps_longitude',src)
        self.assertIn('start_gps_status',src)
        self.assertIn('provinceFeatures=ready',src)
        self.assertIn('BoundaryLookup.mapFeatures(',src)
        self.assertIn('Survey-start GPS fix',src)
        self.assertIn('Saved site reference coordinate',src)
        for forbidden in ('getWritableDatabase(', 'deleteDatabase(', '.execSQL(',
                          'HttpURLConnection','new URL(', 'INTERNET'):
            self.assertNotIn(forbidden,src)
        self.assertIn('new Thread(()->{',src)

    def test_native_land_cover_is_present_without_import(self):
        source_text=source("PaicLandCoverPack.java")
        setup=source("HabitatsSpeciesActivity.java")
        bundled=ROOT/"app/src/main/assets/paic_worldcover2020_500m.txt"
        self.assertTrue(bundled.is_file())
        with bundled.open(encoding="ascii") as file:
            self.assertEqual(file.readline().strip(),
                "LIKAS_WORLDCOVER_2020_500M_V1")
        self.assertIn('BUNDLED="paic_worldcover2020_500m.txt"',source_text)
        self.assertIn('context.getAssets().open(BUNDLED)',source_text)
        self.assertIn('new FileInputStream(source):context.getAssets().open(BUNDLED)',source_text)
        self.assertIn('PaicLandCoverPack.suggest(this,preflightLat,preflightLon)',setup)
        self.assertNotIn('Import offline map',setup)
        self.assertNotIn('ACTION_OPEN_DOCUMENT',setup)
        self.assertIn('"Land-cover source"',setup)

    def test_every_map_entry_has_one_destination(self):
        home=source("WelcomeActivity.java")
        atlas=source("LikasLibraryActivity.java")
        self.assertIn('ApoHomeUi.module(this,"03","Offline Map"',home)
        self.assertIn('this::openMap',home)
        self.assertIn('ApoOfflineMapActivity.class',home)
        self.assertIn('ApoOfflineMapActivity.class',atlas)
        self.assertNotIn('ApoHomeUi.preview(this,"Map"',home)
        self.assertNotIn('private void phMapsHub()',home)
        manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
        self.assertIn('android:name=".ApoOfflineMapActivity"',manifest)
        self.assertIn('android:exported="false"',manifest)

    def test_curated_media_and_survey_identity_preserved(self):
        atlas=source("LikasLibraryActivity.java")
        self.assertNotIn('selectSpeciesPhotos()',atlas)
        self.assertNotIn('PHOTO_BULK_PICKER',atlas)
        self.assertIn('PhLibraryPhotoHero.create(',atlas)
        self.assertIn('"Saved species"',atlas)
        self.assertIn('PhLibraryDirectoryActivity.class',atlas)
        record=source("FieldDb.java")
        self.assertIn('super(context,NAME,null,29)',record)
        build=(ROOT/"app/build.gradle").read_text()
        self.assertIn("applicationId 'org.phwildlife.survey.beta08'",build)
        self.assertGreaterEqual(int(re.search(r'versionCode\s+(\d+)',build).group(1)),174)

if __name__=="__main__":unittest.main()
