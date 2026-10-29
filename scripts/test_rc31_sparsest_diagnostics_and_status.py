"""RC31 new evidence contract: 26 reviewed notes, dated dual-status and visual key limits."""
from pathlib import Path
import json
import unittest

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/"app/src/main/assets"
JAVA=ROOT/"app/src/main/java/org/phwildlife/survey"
TOPICS=("Identification","Habitat","Distribution","Natural history","Conservation")
TAXA={"Buceros hydrocorax","Rhabdotorrhinus leucocephalus",
      "Dicaeum retrocinctum","Actenoides hombroni","Kaloula picta",
      "Varanus rasmusseni","Crateromys paulus","Dicaeum kampalili"}

class SourceAndEvidence(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.core=json.loads((ASSETS/"rc18_unified_96_priority_accounts_v1.json").read_text())
        cls.rc30=json.loads((ASSETS/"rc30_reviewed_priority_evidence_v1.json").read_text())
        cls.rc31=json.loads((ASSETS/"rc31_reviewed_sparse_species_assessments_v1.json").read_text())

    def test_original_corpus_and_new_source_pack(self):
        self.assertEqual(self.core["sourceFactCount"],456)
        self.assertEqual(len(self.core["accounts"]),96)
        self.assertEqual(self.rc30["recordCount"],8)
        self.assertEqual(self.rc31["recordCount"],26)
        self.assertEqual(len(self.rc31["records"]),26)
        self.assertEqual({x["scientificName"] for x in self.rc31["records"]},TAXA)
        self.assertEqual(len({(x["scientificName"],x["section"],x["text"]) 
            for x in self.rc31["records"]}),26)

    def test_all_research_additions_have_verifiable_provenance(self):
        names={a["scientificName"] for a in self.core["accounts"]}
        for rec in self.rc31["records"]:
            self.assertIn(rec["scientificName"],names)
            self.assertIn(rec["section"],TOPICS)
            self.assertTrue(rec["url"].startswith("https://"))
            for key in ("label","text","source","sourceType","sourceLocation","studyScope"):
                self.assertTrue(rec.get(key) and rec[key].strip(),(rec,key))
            self.assertTrue(rec.get("sourceYear") is None or
                1800<=rec["sourceYear"]<=2026)
            if rec["sourceType"]=="government_national_conservation_record":
                self.assertEqual(rec["assessmentAuthority"],"DENR")
                self.assertEqual(rec["assessmentEdition"],"DAO 2019-09")
                self.assertEqual(rec["sourceYear"],2019)
            if "assessmentAuthority" in rec:
                self.assertEqual(rec["section"],"Conservation")
                self.assertIn(rec["assessmentAuthority"],("IUCN","DENR"))
        camp=[r for r in self.rc31["records"] if
             r["scientificName"]=="Dicaeum kampalili"]
        self.assertEqual(camp[0]["diagnosticReadiness"],"taxonomy_only")
        self.assertIn("does not provide",camp[0]["text"])
        self.assertFalse(any("observer" in r["sourceType"] for r in self.rc31["records"]))

    def test_strictly_cumulative_topics_and_original_unchanged(self):
        originals=json.loads(json.dumps(self.core))
        joined={a["scientificName"]:a for a in originals["accounts"]}
        known=set()
        for rec in self.rc30["records"]+self.rc31["records"]:
            k=(rec["scientificName"],rec["section"],rec["text"])
            self.assertNotIn(k,known)
            known.add(k)
            facts=joined[rec["scientificName"]]["sections"][rec["section"]]
            self.assertFalse(any(x["text"]==rec["text"] and x["url"]==rec["url"]
                for x in facts))
            facts.append(rec)
        self.assertEqual(self.core["sourceFactCount"],456)
        self.assertEqual(sum(len(v) for a in originals["accounts"]
                             for v in a["sections"].values()),490)
        self.assertEqual(sum(sum(bool(a["sections"][s]) for s in TOPICS)
            for a in originals["accounts"]),316)
        self.assertEqual(sum(all(a["sections"][s] for s in TOPICS)
            for a in originals["accounts"]),17)
        self.assertEqual({s:sum(not a["sections"][s] for a in
            originals["accounts"]) for s in TOPICS},
            {"Identification":0,"Habitat":32,"Distribution":11,
             "Natural history":57,"Conservation":64})

    def test_offline_rendering_and_distinct_legal_standards(self):
        merger=(JAVA/"Rc30SpeciesEvidence.java").read_text()
        self.assertIn("new JSONObject(corpus.toString())",merger)
        self.assertIn('readPack(activity,FILE,8)',merger)
        self.assertIn('readPack(activity,RC31,26)',merger)
        self.assertIn('rc31ReviewedAdditions',merger)
        for name in ("Rc11PrioritySpeciesView.java",
                     "Rc8TraitComparisonActivity.java",
                     "Rc24ConservationDashboardActivity.java"):
            self.assertIn("Rc30SpeciesEvidence.merge(",(JAVA/name).read_text())
        conservation=(JAVA/"Rc24ConservationDashboardActivity.java").read_text()
        self.assertIn('isDatedIucn(kind)',conservation)
        self.assertIn('"government_national_conservation_record"',conservation)
        catalog=(JAVA/"VerifiedApaCatalog.java").read_text()
        self.assertEqual(catalog.count('m.put("https://'),33)
        for rec in self.rc31["records"]:
            self.assertIn(rec["url"],catalog)
        self.assertIn("versionCode 94",(ROOT/"app/build.gradle").read_text())

if __name__=="__main__":
    unittest.main()
