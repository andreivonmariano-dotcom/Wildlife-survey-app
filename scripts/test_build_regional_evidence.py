import unittest
from build_regional_evidence import extract
class RegionalEvidenceTests(unittest.TestCase):
    def test_province_only_from_explicit_text(self):
        rows=[dict(taxon_key="SY:A",taxon_group="Amphibians",
            source="Sy",source_version="2025-1",
            distribution_text="Mindanao (Davao del Sur), Panay")]
        result=extract(rows)
        self.assertEqual(len(result),2)
        self.assertEqual({x["area_label"] for x in result},
            {"Mindanao","Davao del Sur"})
        self.assertEqual({x["review_status"] for x in result},{"source_listed"})
    def test_no_blanket_davao_inference_from_mindanao(self):
        rows=[dict(taxon_key="SY:B",taxon_group="Reptiles",
            source="Sy",source_version="2026-1",
            distribution_text="Mindanao")]
        result=extract(rows)
        self.assertEqual(len(result),1)
        self.assertEqual(result[0]["area_label"],"Mindanao")
    def test_outside_scope_does_not_become_mindanao(self):
        rows=[dict(taxon_key="SY:C",taxon_group="Reptiles",
            source="Sy",source_version="2026-1",
            distribution_text="Palawan, Busuanga")]
        self.assertEqual(extract(rows),[])
if __name__=="__main__":unittest.main()
