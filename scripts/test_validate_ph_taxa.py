#!/usr/bin/env python3
import csv
import io
import unittest
from validate_ph_taxa import FIELDS, validate

AUTHORITY="Test authority"
VERSION="test-v1"

def sample_rows(rows):
    out=io.StringIO()
    writer=csv.DictWriter(out,fieldnames=FIELDS)
    writer.writeheader()
    writer.writerows(rows)
    return out.getvalue().encode("utf-8")

def row(key,name,region=""):
    return dict(taxon_key=key,taxon_group="Amphibians",
        common_name="",scientific_name=name,family="",
        source=AUTHORITY,source_version=VERSION,region_codes=region,
        review_status="unreviewed")

class ChecklistValidationTests(unittest.TestCase):
    def test_valid_authority_attribution_and_stable_sort(self):
        rows=validate(sample_rows([row("X:2","Testus zeta"),
                                   row("X:1","Testus alpha")]),
                      "Amphibians",AUTHORITY,VERSION)
        self.assertEqual(["X:1","X:2"],[x["taxon_key"] for x in rows])
    def test_reject_duplicate_scientific_names(self):
        with self.assertRaisesRegex(ValueError,"Duplicate scientific"):
            validate(sample_rows([row("X:1","Testus alpha"),
                                  row("X:2","testus Alpha")]),
                    "Amphibians",AUTHORITY,VERSION)
    def test_reject_duplicate_source_identifiers(self):
        with self.assertRaisesRegex(ValueError,"Duplicate taxon key"):
            validate(sample_rows([row("X:1","Testus alpha"),
                                  row("X:1","Testus beta")]),
                    "Amphibians",AUTHORITY,VERSION)
    def test_do_not_infer_or_accept_unverified_province(self):
        with self.assertRaisesRegex(ValueError,"Unverified province"):
            validate(sample_rows([row("X:1","Testus alpha","MINDANAO")]),
                    "Amphibians",AUTHORITY,VERSION)
    def test_reject_source_version_mismatch(self):
        with self.assertRaisesRegex(ValueError,"mismatched citation"):
            validate(sample_rows([row("X:1","Testus alpha")]),
                    "Amphibians",AUTHORITY,"test-v2")
    def test_reject_empty_or_partial_input(self):
        with self.assertRaises(ValueError):
            validate(sample_rows([]),"Amphibians",AUTHORITY,VERSION)
        with self.assertRaises(ValueError):
            validate(b"scientific_name\nTestus alpha\n",
                     "Amphibians",AUTHORITY,VERSION)

if __name__=="__main__":
    unittest.main()
