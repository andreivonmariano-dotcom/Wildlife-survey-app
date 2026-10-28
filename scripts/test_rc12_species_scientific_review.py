"""RC12 scientific publication pilot, twelve-gate and no-false-approval contracts."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"

class ScientificReviewRegisterContract(unittest.TestCase):
    def test_register_matches_unchanged_original_readiness(self):
        register=json.loads((ASSETS/"rc12_species_scientific_review_register_v1.json").read_text())
        source=json.loads((ASSETS/"rc9_profile_completion_v1.json").read_text())
        original={p["scientificName"]:p for p in source["profiles"]}
        fields=[f["id"] for f in source["requirementDefinitions"]]
        self.assertEqual(12,len(fields))
        self.assertEqual(96,register["profileCount"])
        self.assertEqual(0,register["expertApprovedCount"])
        self.assertEqual("rc12-species-scientific-review-register-v1",register["schemaVersion"])
        self.assertEqual(96,len(register["profiles"]))
        self.assertEqual(96,len(original))
        seen=set()
        for row in register["profiles"]:
            name=row["scientificName"]
            self.assertIn(name,original)
            self.assertNotIn(name,seen)
            seen.add(name)
            prior=original[name]
            self.assertEqual(row["sourceTaxonKey"],prior["sourceTaxonKey"])
            self.assertEqual(row["group"],prior["group"])
            self.assertEqual(set(row["fieldEvidence"]),set(fields))
            self.assertEqual(row["reviewState"],"pending_independent_taxon_specialist")
            for key in fields:
                state=row["fieldEvidence"][key]
                self.assertEqual(state["sourceState"],prior["requirements"][key]["status"])
                self.assertEqual(state["existingEvidenceCount"],prior["requirements"][key]["count"])
                self.assertIs(state["expertApproved"],False)
            for key in ("diagnosticKeyApproved","nationalDenrStatusIndependentlyVerified",
                        "globalIucnAssessmentIndependentlyVerified","localOccurrenceConfirmed"):
                self.assertIs(row[key],False)
            self.assertIsNone(row["expertSignoff"])
        self.assertEqual({"Birds":24,"Amphibians":24,"Reptiles":24,"Mammals":24},
            {group:sum(x["group"]==group for x in register["profiles"])
             for group in ("Birds","Amphibians","Reptiles","Mammals")})

    def test_original_description_claims_are_source_traced(self):
        data=json.loads((ASSETS/"rc12_primary_diagnoses_v1.json").read_text())
        review=json.loads((ASSETS/"rc12_species_scientific_review_register_v1.json").read_text())
        self.assertEqual(data["schemaVersion"],"rc12-primary-diagnosis-v1")
        self.assertEqual(3,data["taxonCount"])
        self.assertEqual(16,data["claimCount"])
        self.assertEqual({"Baletemys kampalili","Limnonectes beloncioi","Varanus mabitang"},
                         {x["scientificName"] for x in data["records"]})
        r={x["scientificName"]:x for x in review["profiles"]}
        for species in data["records"]:
            name=species["scientificName"]
            self.assertTrue(species["url"].startswith("https://"))
            self.assertTrue(species["originalPublicationUrl"].startswith("https://"))
            self.assertEqual(len(species["items"]),len(r[name]["originalDescriptionClaimIds"]))
            self.assertIn("pending",species["reviewStatus"])
            for fact in species["items"]:
                self.assertIn(fact["id"],r[name]["originalDescriptionClaimIds"])
                for key in ("sourceLocation","evidenceType","limitation","source","statement"):
                    self.assertTrue(fact[key],(name,key))
                self.assertTrue(fact["url"].startswith("https://"))
                self.assertIn("pending",fact["reviewState"])
        reptile=next(x for x in data["records"] if x["scientificName"]=="Varanus mabitang")
        self.assertEqual("published_diagnosis_secondary_transcription",reptile["evidenceType"])

    def test_compact_reader_includes_source_research_without_approval(self):
        ui=(JAVA/"Rc11PrioritySpeciesView.java").read_text()
        review=(JAVA/"Rc12ScientificReview.java").read_text()
        self.assertIn('rc12_primary_diagnoses_v1.json',ui)
        self.assertIn("Rc12ScientificReview.add(a,references,scientific)",ui)
        self.assertIn("Scientific checklist",review)
        self.assertIn('optInt("expertApprovedCount",-1)!=0',review)
        self.assertIn('rc12_species_scientific_review_register_v1.json',review)
        self.assertIn("setVisibility(View.GONE)",review)

if __name__=="__main__":
    unittest.main()
