import unittest
from check_living_atlas_data import approved_habitat_link, compare_checklist_csv, normalize


class LivingAtlasSourcePolicyTest(unittest.TestCase):
    def test_edition_scoped_name_normalization_never_merges_taxa(self):
        self.assertEqual(normalize(" Oreophryne   cf. nana "), "oreophryne cf. nana")
        self.assertNotEqual(normalize("Oreophryne cf. nana"),
                            normalize("Aphantophryne nana"))

    def test_pending_habitat_evidence_is_excluded(self):
        pack = {"reviewStatus": "source_excerpt_checked_crosswalk_pending_specialist_review"}
        edge = {"scientificReview": "approved", "habitatMappingReview": "approved",
                "siteOccurrence": False}
        self.assertFalse(approved_habitat_link(pack,edge))
        pack["reviewStatus"] = "approved"
        self.assertTrue(approved_habitat_link(pack,edge))
        edge["habitatMappingReview"] = "provisional_physiognomic_crosswalk"
        self.assertFalse(approved_habitat_link(pack,edge))
        edge["habitatMappingReview"] = "approved"
        edge["siteOccurrence"] = True
        self.assertFalse(approved_habitat_link(pack,edge))

    def test_spreadsheet_export_reveals_unmatched_names_without_auto_merging(self):
        csv_text = ("Species Name,Common Name\n"
                    "Pithecophaga jefferyi,Philippine Eagle\n"
                    "Aphantophryne nana,Example\n")
        data = compare_checklist_csv(csv_text,
            ["pithecophaga jefferyi","oreophryne cf. nana"])
        self.assertEqual(data["checklistCount"],2)
        self.assertEqual(data["bookOnly"],["oreophryne cf. nana"])
        self.assertEqual(data["checklistOnly"],["aphantophryne nana"])
        self.assertEqual(data["status"],"requires_editorial_crosswalk")

    def test_missing_source_column_is_error(self):
        with self.assertRaises(ValueError):
            compare_checklist_csv("Name\nPithecophaga jefferyi\n",[])


if __name__ == "__main__":
    unittest.main()
