"""Validate source-backed offline Philippine biodiversity content and licensing."""
from pathlib import Path
import json
import unittest

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class OfflineBiodiversityReference(unittest.TestCase):
    def test_habitat_library_has_citations_and_explicit_field_measurements(self):
        pack=json.loads((ASSETS/"offline_habitat_profiles_v1.json").read_text())
        source_ids={x["id"] for x in pack["sources"]}
        self.assertGreaterEqual(len(pack["profiles"]),9)
        for item in pack["profiles"]:
            self.assertTrue(item["overview"] and item["fieldIndicators"])
            self.assertTrue(item["waterAndSubstrate"])
            self.assertTrue(item["vegetationAndWildlife"])
            self.assertGreaterEqual(len(item["measurements"]),4)
            self.assertTrue(item["limitations"])
            self.assertTrue(item["sourceIds"])
            for source in item["sourceIds"]:self.assertIn(source,source_ids)
        self.assertIn("not a formally validated",pack["reviewLevel"])

    def test_species_accounts_add_ecology_and_use_existing_source_list(self):
        pack=json.loads((ASSETS/"diagnostic_profiles_v1.json").read_text())
        ids={x["id"] for x in pack["sourceList"]}
        self.assertEqual(8,len(pack["profiles"]))
        for profile in pack["profiles"]:
            account=profile["account"]
            for field in ["overview","habitat","behavior","fieldNotes","cautions"]:
                self.assertGreater(len(account[field]),60)
            for source in account["sourceIds"]:self.assertIn(source,ids)
        self.assertIn("not a verified key",pack["scope"])

    def test_photos_are_licensed_and_never_assume_sighting(self):
        media=json.loads((ASSETS/"offline_photo_manifest_v1.json").read_text())
        self.assertGreaterEqual(len(media["items"]),5)
        for item in media["items"]:
            self.assertTrue(item["license"].startswith("CC BY"))
            self.assertTrue(item["licenseUrl"].startswith("https://creativecommons.org/"))
            self.assertTrue(item["sourceUrl"].startswith(
                "https://commons.wikimedia.org/wiki/File:"))
            self.assertTrue(item["author"])
            self.assertTrue(item["offlineAsset"].startswith("offline_photos/"))
        s=(JAVA/"OfflinePhotoEvidence.java").read_text()
        self.assertIn('getAssets().open(file)',s)
        self.assertNotIn("HttpURLConnection",s)
        self.assertIn("licenseUrl",s)
        launch=(JAVA/"LikasLaunchActivity.java").read_text()
        self.assertNotIn("LikasForestView",launch)
        self.assertIn("areAnimatorsEnabled()",launch)
        self.assertIn("finish();",launch)

    def test_reference_screens_are_read_only(self):
        for cls in ["OfflineFieldLibrary.java","OfflinePhotoEvidence.java","HabitatRecordsActivity.java"]:
            s=(JAVA/cls).read_text()
            self.assertNotIn("getWritableDatabase()",s)
            self.assertNotIn("INSERT INTO",s)
        self.assertIn('OfflineFieldLibrary.addSpecies(this,content,scientific)',
            (JAVA/"TaxonDetailActivity.java").read_text())
        self.assertIn("OfflineFieldLibrary.addHabitats(this,items)",
            (JAVA/"HabitatRecordsActivity.java").read_text())
        self.assertIn("citations", (JAVA/"OfflineFieldLibrary.java").read_text().lower()+
            "citations")  # citation provider is part of the view

if __name__=="__main__":unittest.main()
