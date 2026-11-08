#!/usr/bin/env python3
"""Convert the owner-locked PHLibrary Book 01 Word manuscript into a reproducible
offline reader pack. Does not modify source Word, taxonomic catalog, or surveys.
"""
import argparse
import base64
from collections import Counter
import gzip
import hashlib
import json
from pathlib import Path
import re
import sys

from docx import Document

GROUP_FROM_PART = {'Birds':'Birds','Amphibians':'Amphibians','Reptiles':'Reptiles','Mammals':'Mammals'}
COUNTS = {'Birds':409,'Amphibians':53,'Reptiles':154,'Mammals':99}
RANKS = {'Common':124,'Uncommon':336,'Rare':214,'Exceptionally rare':37,'Legendary':3,'Unranked':1}
RANK_LABELS = set(RANKS)
SOURCE_SHA = 'a35f0a53e0586a071c1bd9ff807150643313982503693cd06ea8905f7aeaa279'
RARITY_SHA = '2f0756f5a662e1ea33ac069cf3d7c805949d5568f164198807cb615c1ccd87cc'
CITATION_GROUP = re.compile(r'\(([^()]{1,175}?(?:\b(?:18|19|20)\d{2}[a-z]?|n\.d\.)[^()]*)\)')
DOI_URL = re.compile(r'https?://\S+')
TIER = re.compile(r'✦\s*(Common|Uncommon|Rare|Exceptionally rare|Legendary)(?:\s|$)')


def sha256(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def citations(paragraph):
    output=[]; seen=set()
    for hit in CITATION_GROUP.finditer(paragraph):
        for part in hit.group(1).split(';'):
            s=part.strip()
            if len(s)<4 or len(s)>150 or not re.search(r'\b(?:18|19|20)\d{2}[a-z]?|n\.d\.', s):
                continue
            # In-text citations are preserved even when bibliographic metadata are missing.
            if s not in seen: output.append({'short':s,'full':'','url':'','resolution':'unresolved'});seen.add(s)
    return output


def extract(source):
    doc=Document(source)
    current=None
    records=[]; refs=[]; chapters=[]; expecting=None
    for i,p in enumerate(doc.paragraphs):
        name=p.style.name
        if name=='Heading 1' and p.text in GROUP_FROM_PART:
            current=p.text; chapters.append(current)
        elif name=='Heading 4':
            if current is None:raise ValueError(f'Account before taxonomic chapter at paragraph {i}')
            m=re.fullmatch(r'(.+?)\s+·\s+(.+)',p.text)
            if m is None:raise ValueError(f'Invalid account header: {p.text}')
            records.append({'s':m.group(1),'c':m.group(2),'g':current})
            expecting=['Book Meta','Badge Strip','Article Text']
        elif name in ('Book Meta','Badge Strip','Article Text'):
            if not records or not expecting or name!=expecting.pop(0):
                raise ValueError(f'Account element not aligned: {i}, {name}')
            target=records[-1]
            if name=='Book Meta':
                m=re.fullmatch(r'Order:\s*(.+?)\s*[•]\s*Family:\s*(.+)',p.text)
                if not m:raise ValueError(f'Invalid taxonomy metadata {target["s"]}: {p.text}')
                target['o']=m.group(1).strip();target['f']=m.group(2).strip()
            elif name=='Badge Strip':
                target['b']=p.text
                m=TIER.search(p.text)
                target['t']=m.group(1) if m else 'Unranked'
                # The badges contain a compact source snapshot; keep its original text intact.
                z=re.search(r'\bIUCN\s+([A-Z]{2})\b',p.text)
                target['iucn']=z.group(1) if z else None
            else:
                target['p']=p.text
                target['r']=citations(p.text)
        elif name=='Reference':
            if p.text.strip():
                urls=DOI_URL.findall(p.text)
                refs.append({'text':p.text,'url':urls[0].rstrip('.,)') if urls else None})
    if expecting: raise ValueError('Last account incomplete')
    if chapters!=['Birds','Amphibians','Reptiles','Mammals']:
        raise ValueError(f'Unexpected chapter order {chapters}')
    return records,refs


def validate(records):
    if len(records)!=715:raise ValueError('Expected 715 accounts')
    if Counter(x['g'] for x in records)!=COUNTS:raise ValueError('Group counts mismatch')
    if Counter(x['t'] for x in records)!=RANKS:raise ValueError('Rarity tier counts mismatch')
    names=[r['s'].casefold() for r in records]
    if len(set(names))!=715:raise ValueError('Duplicate names in book')
    for r in records:
        for k in ['s','c','o','f','p']:
            if not r[k].strip():raise ValueError('Blank field '+r['s']+' '+k)
        if '\n' in r['p']:raise ValueError('Article is not one paragraph '+r['s'])
        if 'associated with' in r['p'].lower():raise ValueError('Unrevised habitat phrase '+r['s'])
    taxa={r['s']:r for r in records}
    expected={'Oreophryne cf. nana':'Unranked','Ceyx melanurus':'Rare','Ceyx mindanensis':'Uncommon'}
    for name,tier in expected.items():
        if name not in taxa or taxa[name]['t']!=tier:raise ValueError('Missing/split taxon '+name)
    if 'Aphantophryne nana' in taxa:raise ValueError('Old Camiguin taxon must be only an alias')
    if taxa['Oreophryne cf. nana']['iucn'] is not None:
        raise ValueError('Provisional identification inherited IUCN category')
    return taxa


def rarity_crosscheck(file, records):
    from artifact_tool import Blob, SpreadsheetFile
    wb=SpreadsheetFile.import_xlsx(Blob.load(str(file)))
    sh=wb.worksheets.get_item('All 715 current')
    rows=sh.get_range('A5:M719').values
    if len(rows)!=715:raise ValueError('Companion worksheet row count differs')
    for idx,(r,w) in enumerate(zip(records,rows),1):
        if w[2]!=r['s'] or w[1]!=r['g'] or w[8]!=r['t']:
            raise ValueError(f'Companion does not match Word row {idx}: {r["s"]} / {w[:9]}')


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--book',required=True,type=Path)
    parser.add_argument('--rarity',required=True,type=Path)
    parser.add_argument('--out',required=True,type=Path)
    parser.add_argument('--allow-other-sources',action='store_true',help='testing only; disable locked SHA checks')
    args=parser.parse_args()
    if not args.allow_other_sources:
        if sha256(args.book)!=SOURCE_SHA:raise ValueError('Canonical DOCX SHA-256 mismatch')
        if sha256(args.rarity)!=RARITY_SHA:raise ValueError('Canonical rarity XLSX SHA-256 mismatch')
    records,refs=extract(args.book);validate(records)
    rarity_crosscheck(args.rarity,records)
    args.out.mkdir(parents=True,exist_ok=True)
    aliases={
        'edition':'1.82','conceptVersion':'0.3.4',
        # Crosswalk entries explicitly prohibit automatic re-identification of past field records.
        'crosswalks':[
            {'historical':'Aphantophryne nana','current':'Oreophryne cf. nana','type':'provisional-regional-identification','autoMapObservations':False,'note':'Historical Camiguin species concept is not a synonym of provisional northeastern Mindanao frogs.'},
            {'historical':'Ceyx melanurus (broad historical concept)','currentOptions':['Ceyx melanurus','Ceyx mindanensis'],'type':'taxon-split','autoMapObservations':False},
        ]
    }
    pack={'edition':'1.82','rarityModel':'0.3.4','speciesCount':715,'groups':COUNTS,'rarityCounts':RANKS,'records':records}
    catalog={'edition':'1.82','bibliography':refs}
    def dump(name,obj):
        path=args.out/name
        path.write_bytes((json.dumps(obj,ensure_ascii=False,separators=(',',':'))+'\n').encode('utf-8'))
        return {'file':name,'bytes':path.stat().st_size,'sha256':sha256(path)}
    artifacts=[dump('phlibrary_book01_v182.json',pack),dump('phlibrary_book01_bibliography_v182.json',catalog),dump('phlibrary_book01_taxon_crosswalk_v182.json',aliases)]
    original=(args.out/'phlibrary_book01_v182.json').read_bytes()
    gzip_path=args.out/'phlibrary_book01_v182.json.gz'
    # Deterministic output: fixed gzip mtime, filename suppressed.
    import io
    with gzip.GzipFile(filename='',mode='wb',fileobj=(buf:=io.BytesIO()),compresslevel=9,mtime=0) as gz:gz.write(original)
    gzip_path.write_bytes(buf.getvalue())
    artifacts.append({'file':gzip_path.name,'bytes':gzip_path.stat().st_size,'sha256':sha256(gzip_path)})
    # Binary byte-perfect transfer through a later content-addressed connector.
    # Create 160-column base64 lines to prevent very long indexed lines.
    b64=base64.b64encode(gzip_path.read_bytes()).decode('ascii')
    (args.out/'phlibrary_book01_v182_gzip_transport.txt').write_text('\n'.join(b64[i:i+160] for i in range(0,len(b64),160))+'\n')
    artifacts.append({'file':'phlibrary_book01_v182_gzip_transport.txt','bytes':(args.out/'phlibrary_book01_v182_gzip_transport.txt').stat().st_size,'sha256':sha256(args.out/'phlibrary_book01_v182_gzip_transport.txt')})
    manifest={'edition':'1.82','rarityModel':'0.3.4','sourceDocxSha256':SOURCE_SHA,'sourceRarityXlsxSha256':RARITY_SHA,
              'groups':COUNTS,'rarityCounts':RANKS,'speciesCount':715,'bibliographyEntries':len(refs),
              'citationFragments':sum(len(r['r']) for r in records), 'citationResolution':'Most text references are unresolved; retain full book bibliography as an independent source index',
              'files':artifacts, 'notes': ['Field-guide habitat synthesis may include inferred ecology.','Old observations are never silently reassigned across split taxon concepts.']}
    dump('phlibrary_book01_v182_manifest.json',manifest)
    print(json.dumps(manifest,indent=2))

if __name__=='__main__':main()
