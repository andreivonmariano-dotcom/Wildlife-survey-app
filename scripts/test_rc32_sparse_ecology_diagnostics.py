"""RC32 preservation and accuracy gates for 15 reviewed sparse records and paired keys."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/"app/src/main/assets"
SRC=ROOT/"app/src/main/java/org/phwildlife/survey"
TOPICS=("Identification","Habitat","Distribution","Natural history","Conservation")

def load(name):
    return json.loads((DATA/name).read_text(encoding="utf-8"))

class Rc32Science(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.original=load("rc18_unified_96_priority_accounts_v1.json")
        cls.packs=[load("rc30_reviewed_priority_evidence_v1.json"),
                   load("rc31_reviewed_sparse_species_assessments_v1.json"),
                   load("rc32_sparse_priority_ecology_diagnostics_v1.json")]
        cls.comparisons=load("rc9_differential_traits_v1.json")

    def test_readonly_core_96_with_exact_15_new(self):
        self.assertEqual(self.original["sourceFactCount"],456)
        self.assertEqual(len(self.original["accounts"]),96)
        self.assertEqual([p["recordCount"] for p in self.packs],[8,26,15])
        self.assertEqual([len(p["records"]) for p in self.packs],[8,26,15])
        new=self.packs[2]["records"]
        self.assertEqual(len(set(x["scientificName"] for x in new)),6)
        names={a["scientificName"] for a in self.original["accounts"]}
        for r in new:
            self.assertIn(r["scientificName"],names)
            self.assertIn(r["section"],TOPICS)
            for k in ("label","text","url","source","sourceType","sourceLocation","studyScope"):
                self.assertTrue(str(r.get(k,"")).strip(),(r,k))
            self.assertTrue(r["url"].startswith("https://"))
            if r.get("assessmentAuthority")=="DENR":
                self.assertEqual(r["assessmentEdition"],"DAO 2019-09")
                self.assertEqual(r["section"],"Conservation")
            if r.get("assessmentGlobalEdition"):
                self.assertEqual(r["assessmentVerification"],"specialist_group_secondary_only")
            if r.get("diagnosticReadiness"):
                self.assertEqual(r["section"],"Identification")

    def test_overlay_does_not_double_count_original_records(self):
        updated=json.loads(json.dumps(self.original))
        by_name={a["scientificName"]:a for a in updated["accounts"]}
        keys=set()
        for pack in self.packs:
            for r in pack["records"]:
                key=(r["scientificName"],r["section"],r["text"])
                self.assertNotIn(key,keys)
                keys.add(key)
                section=by_name[r["scientificName"]]["sections"][r["section"]]
                self.assertFalse(any(x["text"]==r["text"] and x["url"]==r["url"]
                                 for x in section),key)
                section.append(r)
        self.assertEqual(sum(len(a["sections"][s]) for a in updated["accounts"]
                             for s in TOPICS),505)
        counts=[sum(bool(a["sections"][s]) for s in TOPICS)
                for a in updated["accounts"]]
        self.assertEqual(sum(counts),325)
        self.assertEqual(sum(k==5 for k in counts),18)
        self.assertEqual(sum(k<=2 for k in counts),23)
        self.assertEqual({s:sum(not a["sections"][s] for a in updated["accounts"])
                          for s in TOPICS},
                         {"Identification":0,"Habitat":29,"Distribution":9,
                          "Natural history":56,"Conservation":61})

    def test_dated_status_and_source_catalog(self):
        apa=(SRC/"VerifiedApaCatalog.java").read_text()
        self.assertEqual(apa.count('m.put("https://'),33)
        for r in self.packs[2]["records"]:
            self.assertIn(r["url"],apa)
        assert 'readPack(activity,RC32,15)' in (SRC/"Rc30SpeciesEvidence.java").read_text()
        assert 'rc32ReviewedAdditions' in (SRC/"Rc30SpeciesEvidence.java").read_text()
        self.assertIn("versionCode 92",(ROOT/"app/build.gradle").read_text())

    def test_two_new_original_study_comparisons(self):
        pairs=self.comparisons["pairs"]
        self.assertEqual(self.comparisons["pairCount"],len(pairs))
        self.assertEqual(len(pairs),6)
        comparisons={p.get("id"):p for p in pairs}
        for name in ("RC32-AGUSAN-GUBAOT","RC32-LUZON-COBRA"):
            self.assertIn(name,comparisons)
            self.assertEqual(len(comparisons[name]["evidence"]),4)
            for e in comparisons[name]["evidence"]:
                self.assertEqual(e["sourceA"],e["sourceB"])
                self.assertIn(e["sourceA"],apa_from_sources())
                self.assertTrue(e["caution"].strip())
                self.assertTrue(e["stage"].strip())
        self.assertIn("not",comparisons["RC32-LUZON-COBRA"]["review"])

def apa_from_sources():
    return (SRC/"VerifiedApaCatalog.java").read_text()

if __name__=="__main__":
    unittest.main()
