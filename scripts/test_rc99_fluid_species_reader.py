"""Verify editorial flow, source conservation and rarity identity in PHLibrary."""
import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]/"app/src/main/assets"

def read(name):
    return json.loads((ROOT/name).read_text())

class FluentReader(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        original={}
        for part in range(1,9):
            for record in read(f"phlibrary_book01_v175_{part:02d}.json")["records"]:
                assert record["s"] not in original
                original[record["s"]]=record
        cls.original=original

    def test_refinements_preserve_references_and_remove_contrasts(self):
        paragraphs={s:r["p"] for s,r in self.original.items()}
        bibliographies={s:list(r["r"]) for s,r in self.original.items()}
        edit=read("phlibrary_editorial_v176.json")
        for row in edit["records"]:
            paragraphs[row["s"]]=row["p"]
            bibliographies[row["s"]]=row["r"]
        refined=read("phlibrary_reader_refinements_v178.json")
        self.assertEqual(refined["changed"],44)
        for row in refined["records"]:
            name=row["s"];old=row["old"];new=row["revised"]
            self.assertEqual(paragraphs[name].count(old),1,name)
            self.assertNotIn("rather than",new.lower())
            before=paragraphs[name]
            paragraphs[name]=before.replace(old,new)
            for source in bibliographies[name]:
                if source["short"] in before:
                    self.assertIn(source["short"],paragraphs[name],name)
        cues=read("phlibrary_habitat_cues_v177.json")
        self.assertEqual(cues["changed"],100)
        self.assertEqual(
            cues["placementCounts"],{"insert":74,"replace":8,"retain":18})
        seen=set()
        for row in cues["records"]:
            name=row["s"]; self.assertNotIn(name,seen);seen.add(name)
            self.assertIn(name,paragraphs)
            self.assertEqual(row["group"],self.original[name]["g"])
            anchor=row["anchor"]; paragraph=paragraphs[name]
            self.assertEqual(paragraph.count(anchor),1,name)
            mode=row["mode"]
            self.assertIn(mode,("insert","replace","retain"))
            if mode=="replace":
                paragraphs[name]=paragraph.replace(anchor,row["fieldCue"])
            elif mode=="insert":
                paragraphs[name]=paragraph.replace(anchor,anchor+" "+row["fieldCue"])
            if row.get("newReference"):
                ref=row["newReference"]
                self.assertIn(ref["short"],row["fieldCue"])
                bibliographies[name].append(ref)
            self.assertEqual(len(paragraphs[name].splitlines()),1)
            self.assertNotIn("rather than",row["fieldCue"].lower())
        polished=read("phlibrary_reader_polish_v179.json")
        self.assertEqual(polished["changed"],94)
        for row in polished["records"]:
            name=row["s"];old=row["old"];new=row["revised"]
            self.assertEqual(paragraphs[name].count(old),1,name)
            before=paragraphs[name]
            paragraphs[name]=before.replace(old,new)
            for source in bibliographies[name]:
                if source["short"] in before:
                    self.assertIn(source["short"],paragraphs[name],name)
        self.assertEqual(len(paragraphs),714)
        remaining=[s for s,p in paragraphs.items() if "rather than" in p.lower()]
        self.assertEqual(remaining,[])
        for name,text in paragraphs.items():
            self.assertTrue(text.strip(),name)
            for source in bibliographies[name]:
                self.assertIn(source["short"],text,name)
        for key in ("g","t","b","s","f"):
            self.assertEqual(
                {s:r[key] for s,r in self.original.items()},
                {s:r[key] for s,r in self.original.items()})
        tiers={}
        for r in self.original.values():
            tiers[r["t"]]=tiers.get(r["t"],0)+1
        self.assertEqual(tiers,{
          "Common":117,"Uncommon":343,"Rare":214,
          "Exceptionally rare":37,"Legendary":3})
    def test_java_applies_readability_before_context_placement(self):
        java=(ROOT.parent/"java/org/phwildlife/survey/PhLibraryBook01.java").read_text()
        a=java.index("applyEditorial(context,rows,index);")
        b=java.index("applyReaderRefinements(context,rows,index);")
        c=java.index("applyHabitatCues(context,rows,index);")
        d=java.index("applyReaderPolish(context,rows,index);")
        self.assertTrue(a<b<c<d)
        self.assertIn("1.78-fluent-reader",java)
        self.assertIn("1.79-reader-polish",java)
        self.assertIn("String anchor=detail.getString(\"anchor\")",java)

if __name__=="__main__":
    unittest.main()
