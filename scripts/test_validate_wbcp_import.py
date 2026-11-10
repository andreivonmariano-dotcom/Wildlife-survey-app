import csv
import tempfile
import unittest
from pathlib import Path
from validate_wbcp_import import inspect

HEADERS=["taxon_key","taxon_group","common_name","scientific_name","family",
"source","source_version","region_codes","review_status","distribution_status",
"source_url","ebird_url","sound_url"]

def fake_rows():
    rows=[]
    for i in range(761):
        rows.append(dict(taxon_key=f"WBCP2025:Genus_speciesa{i}",
            taxon_group="Birds",common_name=f"Test bird {i}",
            scientific_name=f"Genus speciesa{i}",
            family="Anatidae",source="Wild Bird Club of the Philippines (test)",
            source_version="2025-04-24_IOC15.1",
            region_codes="MINDANAO" if i<179 else "",
            review_status="reviewed",
            distribution_status="Endemic" if i<274 else "Resident",
            source_url="https://birdwatch.ph/philippine-checklist/",
            ebird_url="https://ebird.org/species/test",
            sound_url="https://www.xeno-canto.org/species/Genus-speciesa"))
    return rows

class ImportValidationTests(unittest.TestCase):
    def validate(self,rows):
        with tempfile.TemporaryDirectory() as tmp:
            p=Path(tmp)/"birds.csv"
            with p.open("w",encoding="utf-8",newline="") as out:
                writer=csv.DictWriter(out,fieldnames=HEADERS)
                writer.writeheader();writer.writerows(rows)
            return inspect(p)
    def test_expected_761_and_endemicity(self):
        counts=self.validate(fake_rows())
        self.assertEqual(counts["species"],761)
        self.assertEqual(counts["endemics"],274)
        self.assertEqual(counts["mindanao_mentions"],179)
    def test_missing_name_rejected(self):
        rows=fake_rows();rows[0]["scientific_name"]=""
        with self.assertRaisesRegex(ValueError,"Missing"):
            self.validate(rows)
    def test_duplicate_name_rejected(self):
        rows=fake_rows();rows[2]["scientific_name"]=rows[1]["scientific_name"]
        with self.assertRaisesRegex(ValueError,"Duplicate"):
            self.validate(rows)
    def test_url_safety(self):
        rows=fake_rows();rows[0]["ebird_url"]="https://fake.example/species/"
        with self.assertRaisesRegex(ValueError,"Unexpected external"):
            self.validate(rows)
if __name__=="__main__":unittest.main()
