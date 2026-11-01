"""RC71: public asset presence, scientific source identity, and map integrity."""
import csv
import hashlib
import io
import pathlib
import unittest
import zlib

ROOT=pathlib.Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
MASTER=ASSETS/"paic714_master.csv"
MAP=ASSETS/"paic_worldcover2020_500m.txt"
ATTR=ASSETS/"PAIC_WORLD_COVER_ATTRIBUTION.txt"
MASTER_SHA="1ebf7c4ac50787b2dd6f3c22a484057fbac7e61188101bd2f2002c5ce65de959"
MAP_SHA="e86e0a90096e104f09ee195b70c6cfe44bd6600211a44229973a3a0c80344089"

class PublicBundledReferencesTest(unittest.TestCase):
    def test_exact_owner_authorized_checklist_in_public_assets(self):
        raw=MASTER.read_bytes()
        self.assertEqual(MASTER_SHA,hashlib.sha256(raw).hexdigest())
        rows=list(csv.DictReader(io.StringIO(raw.decode("utf-8"),newline="")))
        self.assertEqual(714,len(rows))
        self.assertEqual(714,len({r["scientific_name"].lower() for r in rows}))
        self.assertEqual({"Birds":408,"Amphibians":53,"Reptiles":154,"Mammals":99},
            {group:sum(r["taxon_group"]==group for r in rows)
             for group in ["Birds","Amphibians","Reptiles","Mammals"]})
        for omitted in ("Apomys camiguinensis","Apus affinis","Brachymeles vulcani",
                        "Bullimus gamay","Hypsipetes catarmanensis",
                        "Loriculus camiguinensis","Miniopterus fuliginosus",
                        "Ninox leventisi","Pseudorabdion mcnamarae",
                        "Sturnus vulgaris","Zosterops nigrorum"):
            self.assertNotIn(omitted,{r["scientific_name"] for r in rows})
        self.assertTrue(all(r["habitat_text"] and r["elevation_text"] for r in rows))
    def test_worldcover_16_tile_map_and_attribution(self):
        raw=MAP.read_bytes()
        self.assertEqual(MAP_SHA,hashlib.sha256(raw).hexdigest())
        self.assertTrue(raw.startswith(b"LIKAS_WORLDCOVER_2020_500M_V1\n"))
        import base64
        tiles=[line for line in raw.decode("ascii").splitlines()
               if line and not line.startswith("#") and "|" in line]
        self.assertEqual(16,len(tiles))
        for record in tiles:
            tile_id,encoded=record.split("|",1)
            self.assertRegex(tile_id,r"^N[0-9]{2}E[0-9]{3}$")
            unpacked=zlib.decompress(base64.b64decode(encoded))
            self.assertEqual(600*600,len(unpacked))
        a=ATTR.read_text()
        self.assertIn("CC BY 4.0",a)
        self.assertIn("10.5281/zenodo.5571936",a)
        self.assertIn("Contains modified Copernicus Sentinel data",a)
    def test_app_bootstraps_public_assets_without_picker(self):
        java=(ROOT/"app/src/main/java/org/phwildlife/survey/"
               "BundledPrivateReferences.java").read_text()
        launch=(ROOT/"app/src/main/java/org/phwildlife/survey/"
                "LikasLaunchActivity.java").read_text()
        gradle=(ROOT/"app/build.gradle").read_text()
        self.assertIn('static final String CATALOG="paic714_master.csv"',java)
        self.assertIn('static final String MAP="paic_worldcover2020_500m.txt"',java)
        self.assertIn("BundledPrivateReferences.ensureInstalled",launch)
        self.assertIn("verifyBundledPaicReferences",gradle)
        self.assertGreaterEqual(int(__import__('re').search(r'versionCode\\s+(\\d+)',gradle).group(1)),131)

if __name__=="__main__":
    unittest.main()
