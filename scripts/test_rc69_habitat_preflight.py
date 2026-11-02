"""RC69 source safety checks. No downloaded or proprietary checklist is bundled."""
import pathlib
import unittest

ROOT=pathlib.Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/org/phwildlife/survey'

def read(name):
    return (JAVA/name).read_text(encoding='utf-8')

class Rc69HabitatPreflightTests(unittest.TestCase):
    def test_optional_preflight_does_not_create_survey(self):
        setup=read('WelcomeActivity.java')
        gps=read('SurveyGpsActivity.java')
        screen=read('HabitatsSpeciesActivity.java')
        self.assertIn('"prepare_field_checklist"',setup)
        self.assertIn('HabitatsSpeciesActivity.class:MainActivity.class',gps)
        self.assertIn('new Intent(this,MainActivity.class)',screen)
        self.assertIn('GpsFixPolicy.acceptableElapsed(',screen)
        self.assertIn('launch.removeExtra("start_gps_latitude")',screen)
        self.assertIn('create_new',screen)
    def test_new_survey_data_and_exports_are_atomic(self):
        main=read('MainActivity.java')
        db=read('FieldDb.java')
        export=read('ResearchExports.java')
        self.assertIn('SurveyHabitatSetup.save(database,eventId',main)
        self.assertIn('super(context,NAME,null,29)',db)
        self.assertIn('if(oldVersion<28)upgradeTwentyEight(d)',db)
        self.assertIn('survey_habitat_preparation.csv',export)
        self.assertIn('confirmed_habitats',export)
    def test_imported_landcover_is_separate_from_observed_habitat(self):
        screen=read('HabitatsSpeciesActivity.java')
        pack=read('PaicLandCoverPack.java')
        self.assertIn('check.setChecked(confirmed.contains(name))',screen)
        self.assertIn('PaicHabitatPolicy.encode(confirmed)',screen)
        self.assertIn('PaicHabitatPolicy.encode(new HashSet<>(suggested))',screen)
        self.assertIn('LIKAS_WORLDCOVER_2020_500M_V1',pack)
        self.assertIn('MAX_BYTES=1024*1024',pack)
        self.assertIn('historical satellite land cover',screen.lower())
        # No 2020 map data or owner-private master CSV in public APK assets.
        self.assertFalse((ROOT/'app/src/main/assets/paic_worldcover_2020_500m_v1.txt').exists())
    def test_legacy_backup_compatibility(self):
        backup=read('FieldBackupArchive.java')
        self.assertIn('version>=27&&version<=29',backup)
        self.assertIn('manifest.put("sqliteSchema",snapshotSchema)',backup)
        self.assertIn('survey_habitat_setup',backup)

if __name__=='__main__':unittest.main()
