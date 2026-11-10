#!/usr/bin/env python3
"""Preflight checks for the versioned private WBCP/Sy Android asset."""
import base64
import hashlib
import json
from pathlib import Path
import unittest
import zlib

ASSET=Path("app/src/main/assets/likas_ph_checklists_v2026_private.b64")
SHA256="7845ceadc4b6d6226781478c72281364c876a4c1ad47b80d21b7d176ab7dc4c6"

class BundledCatalogTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        data=base64.b64decode(ASSET.read_text().strip(),validate=True)
        assert hashlib.sha256(data).hexdigest()==SHA256, "Bundled data was modified"
        cls.rows,cls.evidence=json.loads(zlib.decompress(data))
    def test_all_sources_included(self):
        self.assertEqual(len(self.rows),1257)
        self.assertEqual(len(self.evidence),363)
        bygroup={g:sum(1 for row in self.rows if row[1]==g)
                 for g in ("Birds","Amphibians","Reptiles")}
        self.assertEqual(bygroup,{"Birds":761,"Amphibians":116,"Reptiles":380})
    def test_unique_ids_and_scientific_names(self):
        keys=[r[0] for r in self.rows]
        self.assertEqual(len(set(keys)),len(keys))
        species=[(r[1],r[3].casefold()) for r in self.rows]
        self.assertEqual(len(set(species)),len(species))
    def test_no_orphaned_regional_data(self):
        valid={r[0] for r in self.rows}
        self.assertTrue(all(e[0] in valid for e in self.evidence))
        self.assertTrue(all(e[1] in ("province","island") for e in self.evidence))
    def test_historic_synonyms_kept(self):
        self.assertTrue(any(r[9] for r in self.rows if r[1]=="Amphibians"))
    def test_named_sources_without_mammal_overlap(self):
        self.assertTrue(all(r[1]!="Mammals" for r in self.rows))
        self.assertTrue(all(r[0].startswith("WBCP2025:") for r in self.rows if r[1]=="Birds"))
        self.assertTrue(all(r[0].startswith("SY") for r in self.rows if r[1] in ("Amphibians","Reptiles")))

if __name__=="__main__":
    unittest.main()
