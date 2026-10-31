#!/usr/bin/env python3
"""Synthetic, coordinate-free acceptance tests for PAIC-ECOGUIDE-1.0."""
import unittest
from likas_build_paic_ecoguide_v1 import compile_guide, LITERATURE_FIELDS

def source(name="Testus firstus", n=0, distinct=0, land=0, qa="NO_RECONCILIATION_DELTA"):
    return dict(
        taxon_id="TEST-1", group="Birds", scientific_name=name,
        prepolygon_candidate_count=str(n), inside_major_islands=str(n),
        distinct_public_coordinate_pairs_inside=str(distinct),
        dsm_numeric_count=str(n), dsm_nonnegative_count=str(n),
        dsm_min_m="123.4" if n else "", dsm_p10_m="123.4" if n else "",
        dsm_median_m="123.4" if n else "", dsm_p90_m="456.8" if n else "",
        dsm_max_m="456.8" if n else "",
        worldcover2020_class_count=str(land),
        worldcover2020_classes_observed="Tree cover: 1" if land else "",
        worldcover2020_tree_cover_count=str(land),
        worldcover2020_temporal_mismatch_count=str(land),
        source_qa_status=qa, species_inference_status="NO_SCREENED_RECORDS" if n == 0 else "PROVISIONAL_OBSERVED_ASSOCIATIONS_ONLY",
    )

def paper(habitat="Lower montane forest near moss-covered logs",
          elevation="1640, 1900", citation="Example et al. (2022)"):
    return dict(zip(LITERATURE_FIELDS, [
        "TEST-1", habitat, citation, "https://doi.org/10.1234/example",
        elevation, citation, "https://doi.org/10.1234/example", "Mt Example", "Testus firstus"
    ]))

class GuideCompilerTests(unittest.TestCase):
    def make(self, row, sources=None):
        return compile_guide([row], sources or [], expected_taxa=1)

    def test_no_coordinate_literature_only(self):
        rows, manifest = self.make(source(), [paper()])
        x = rows[0]
        self.assertIn("Lower montane forest", x["habitat_guide"])
        self.assertIn("1640, 1900 m", x["elevation_guide"])
        self.assertNotIn("Raster surface height", x["elevation_guide"])
        self.assertFalse(manifest["requires_specialist_approval"])
        self.assertFalse(manifest["display_generic_provisional_label"])

    def test_habitat_only_never_infers_numeric_elevation(self):
        rows, _ = self.make(source(), [paper(elevation="")])
        self.assertEqual(rows[0]["elevation_guide"], "Not documented in the current sources")

    def test_no_evidence_is_unknown_not_zero(self):
        rows, _ = self.make(source())
        self.assertIn("Not documented", rows[0]["habitat_guide"])
        self.assertIn("Not documented", rows[0]["elevation_guide"])
        self.assertEqual(rows[0]["records_n"], "0")

    def test_screened_records_describe_sample_not_preference(self):
        rows, _ = self.make(source(n=3, distinct=2, land=1))
        x=rows[0]
        self.assertIn("123.4–456.8 m", x["elevation_guide"])
        self.assertIn("2 distinct coordinate pairs, 3 records", x["elevation_guide"])
        self.assertIn("WorldCover 2020", x["habitat_guide"])
        self.assertNotIn("preferred", x["elevation_guide"].lower())

    def test_disputed_raster_withheld_literature_still_available(self):
        rows, manifest = self.make(
            source(n=2, distinct=2, land=1, qa="HELD_59_ROW_RECONCILIATION"), [paper()])
        x = rows[0]
        self.assertEqual(x["records_n"], "0")
        self.assertEqual(x["map_class_records_n"], "0")
        self.assertIn("Published habitat", x["habitat_guide"])
        self.assertIn("Published collection", x["elevation_guide"])
        self.assertIn("reconciliation", x["source_note"])
        self.assertEqual(manifest["source_reconciliation_hold_taxa"], 1)

    def test_no_unattributed_habitat_claim(self):
        bad=paper()
        bad["habitat_citation"]=""
        with self.assertRaises(ValueError):
            self.make(source(),[bad])

    def test_no_unattributed_elevation(self):
        bad=paper()
        bad["published_elevation_reference_url"]=""
        with self.assertRaises(ValueError):
            self.make(source(),[bad])

    def test_source_count_integrity(self):
        with self.assertRaises(ValueError):
            self.make(source(n=1, distinct=5))
        with self.assertRaises(ValueError):
            compile_guide([source(), source()], expected_taxa=2)

    def test_unknown_species_in_literature_rejected(self):
        bad=paper()
        bad["taxon_id"]="NOT-IN-CHECKLIST"
        with self.assertRaises(ValueError):
            self.make(source(), [bad])

if __name__ == "__main__":
    unittest.main()
