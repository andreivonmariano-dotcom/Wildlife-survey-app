"""Source-level regression for the document-reviewed, fully integrated PHLibrary reader."""
import json,unittest
from collections import Counter
from pathlib import Path

ASSETS=Path(__file__).resolve().parents[1]/'app/src/main/assets'

def read(file):return json.loads((ASSETS/file).read_text(encoding='utf8'))

class IntegratedHabitatReader(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        originals={}
        for i in range(1,9):
            for r in read(f'phlibrary_book01_v175_{i:02d}.json')['records']:
                assert r['s'] not in originals,r['s']
                originals[r['s']]=r
        cls.originals=originals

    def test_fluent_merge_preserves_all_source_records_and_citations(self):
        self.assertEqual(len(self.originals),714)
        paragraphs={s:r['p'] for s,r in self.originals.items()}
        refs={s:list(r['r']) for s,r in self.originals.items()}
        for r in read('phlibrary_editorial_v176.json')['records']:
            paragraphs[r['s']]=r['p'];refs[r['s']]=list(r['r'])
        for r in read('phlibrary_reader_refinements_v178.json')['records']:
            p=paragraphs[r['s']];self.assertEqual(p.count(r['old']),1,r['s'])
            paragraphs[r['s']]=p.replace(r['old'],r['revised'])
        pack=read('phlibrary_habitat_cues_v177.json')
        self.assertEqual(len(pack['records']),100)
        self.assertEqual(pack['placementCounts'],{'insert':47,'replace':8,'retain':45})
        for r in pack['records']:
            p=paragraphs[r['s']];a=r['anchor'];self.assertEqual(p.count(a),1,r['s'])
            if r['mode']=='insert':paragraphs[r['s']]=p.replace(a,a+' '+r['fieldCue'])
            if r['mode']=='replace':paragraphs[r['s']]=p.replace(a,r['fieldCue'])
            if r.get('newReference') and not any(x['short']==r['newReference']['short'] for x in refs[r['s']]):refs[r['s']].append(r['newReference'])
        for r in read('phlibrary_reader_polish_v179.json')['records']:
            p=paragraphs[r['s']];self.assertEqual(p.count(r['old']),1,r['s'])
            paragraphs[r['s']]=p.replace(r['old'],r['revised'])
        integrated=read('phlibrary_habitat_integration_v180.json')
        self.assertEqual(integrated['edition'],'1.80-integrated-habitats')
        self.assertEqual(integrated['speciesCount'],714)
        self.assertEqual(integrated['reviewedSpecies'],100)
        self.assertEqual(integrated['integratedSpecies'],47)
        self.assertEqual(integrated['changed'],48)
        changed=set()
        for r in integrated['records']:
            name=r['s'];p=paragraphs[name]
            self.assertEqual(p.count(r['old']),1,name)
            self.assertNotIn('rather than',r['revised'].lower())
            after=p.replace(r['old'],r['revised'])
            for x in refs[name]:
                if x['short'] in p:self.assertIn(x['short'],after,name)
            paragraphs[name]=after;changed.add(name)
        self.assertEqual(len(changed),47)
        self.assertEqual(len(paragraphs),714)
        for name,p in paragraphs.items():
            self.assertNotIn('\n',p,name)
            self.assertNotIn('rather than',p.lower(),name)
            self.assertTrue(p.strip(),name)
            for r in refs[name]:self.assertIn(r['short'],p,name)
        for name in set(paragraphs)-changed:
            self.assertEqual(paragraphs[name],
                # Reconstruct unchanged overlay order to ensure merger only affects 47 species.
                self._before_integration(name),name)
        self.assertIn('On Samar, records include Mount Huraw and Eastern Samar (GBIF.org, 2026;',paragraphs['Gonyosoma oxycephalum'])
        self.assertIn('Published accounts describe the Red-tailed Green Ratsnake as a diurnal',paragraphs['Gonyosoma oxycephalum'])
        self.assertNotIn('Rice paddies, irrigation channels, still pools beside streams and swampy forest patches are documented habitats',paragraphs['Fejervarya vittigera'])
        self.assertIn('In a Luzon GPS study',paragraphs['Acerodon jubatus'])
        self.assertEqual(Counter(r['g'] for r in self.originals.values()),Counter(Birds=408,Amphibians=53,Reptiles=154,Mammals=99))
        self.assertEqual(Counter(r['t'] for r in self.originals.values()),Counter({'Common':117,'Uncommon':343,'Rare':214,'Exceptionally rare':37,'Legendary':3}))

    def _before_integration(self,name):
        r=self.originals[name]
        overlay={x['s']:x for x in read('phlibrary_editorial_v176.json')['records']}
        p=overlay.get(name,r).get('p',r['p'])
        for x in read('phlibrary_reader_refinements_v178.json')['records']:
            if x['s']==name:p=p.replace(x['old'],x['revised'])
        for x in read('phlibrary_habitat_cues_v177.json')['records']:
            if x['s']==name:
                if x['mode']=='insert':p=p.replace(x['anchor'],x['anchor']+' '+x['fieldCue'])
                if x['mode']=='replace':p=p.replace(x['anchor'],x['fieldCue'])
        for x in read('phlibrary_reader_polish_v179.json')['records']:
            if x['s']==name:p=p.replace(x['old'],x['revised'])
        return p

    def test_android_loader_runs_after_all_original_layers(self):
        root=ASSETS.parent/'java/org/phwildlife/survey/PhLibraryBook01.java'
        src=root.read_text()
        names=['applyEditorial(context,rows,index);','applyReaderRefinements(context,rows,index);','applyHabitatCues(context,rows,index);','applyReaderPolish(context,rows,index);','applyHabitatIntegration(context,rows,index);']
        indexes=[src.index(n) for n in names]
        self.assertEqual(indexes,sorted(indexes))
        self.assertIn('Text v1.80',src)
        self.assertIn('Reader v1.80',src)
        self.assertIn('habitat_integration_v180.json',src)

if __name__=='__main__':unittest.main()