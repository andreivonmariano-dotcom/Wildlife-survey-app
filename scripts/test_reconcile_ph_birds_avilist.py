import unittest
from reconcile_ph_birds_avilist import reconcile,norm

class PhilippineBirdCrosswalkTests(unittest.TestCase):
    def test_exact_accepted_name_only(self):
        avi=[{"id":"1001","sci":"Pithecophaga jefferyi",
              "common":"Philippine Eagle","family":"Accipitridae"}]
        ph=[{"scientific":"Pithecophaga jefferyi"},
            {"scientific":"Unmapped ancient name"}]
        matches,review=reconcile(avi,ph,"id","sci","common","family",
            "scientific","WBCP","2025")
        self.assertEqual(len(matches),1)
        self.assertEqual(matches[0]["taxon_key"],"AVILIST2026:1001")
        self.assertEqual(matches[0]["region_codes"],"")
        self.assertEqual(review[0]["match_status"],"unmatched")
    def test_taxonomic_split_or_duplicate_requires_review(self):
        avi=[{"id":"1","sci":"Alpha beta","common":"A","family":"A"},
             {"id":"2","sci":"Alpha beta","common":"B","family":"A"}]
        matches,review=reconcile(avi,[{"scientific":"Alpha beta"}],
            "id","sci","common","family","scientific","WBCP","2025")
        self.assertEqual(len(matches),0)
        self.assertEqual(review[0]["match_status"],"ambiguous")
    def test_duplicate_occurrence_does_not_duplicate_taxon_id(self):
        avi=[{"id":"1","sci":"Alpha beta","common":"A","family":"A"}]
        ph=[{"scientific":"Alpha beta"},{"scientific":"alpha beta"}]
        matches,review=reconcile(avi,ph,
            "id","sci","common","family","scientific","WBCP","2025")
        self.assertEqual(len(matches),1)
        self.assertEqual(review[0]["match_status"],"duplicate")
if __name__=="__main__":
    unittest.main()
