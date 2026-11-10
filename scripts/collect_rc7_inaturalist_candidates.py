#!/usr/bin/env python3
"""Find individually CC-licensed iNaturalist candidates for LIKAS RC7A, no image copies."""
import json,time,urllib.parse,urllib.request,collections,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
accounts=json.loads((ROOT/"app/src/main/assets/rc7a_field_cards_v1.json").read_text())["accounts"]
names=[x["scientificName"] for x in accounts if x["accountTier"]=="source_catalog_priority_draft"]
permitted={"cc0","cc-by","cc-by-sa","cc-by-nc","cc-by-nc-sa"}
data=[]
for k,name in enumerate(names):
  if k:time.sleep(0.45)
  query={"taxon_name":name,"photos":"true","per_page":"8","quality_grade":"research",
    "photo_license":"CC0,CC-BY,CC-BY-SA,CC-BY-NC,CC-BY-NC-SA"}
  url="https://api.inaturalist.org/v1/observations?"+urllib.parse.urlencode(query)
  record={"scientificName":name,"candidates":[]}
  try:
    request=urllib.request.Request(url,headers={"User-Agent":"LIKAS-private-research-audit/1.0","Accept":"application/json"})
    with urllib.request.urlopen(request,timeout=25) as response:
      raw=response.read(6000000)
    res=json.loads(raw)
    record["totalResults"]=res.get("total_results",0)
    seen=set()
    for obs in res.get("results",[]):
      if (obs.get("taxon") or {}).get("name")!=name:continue
      for p in obs.get("photos",[]):
        lic=(p.get("license_code") or "").lower()
        photoid=p.get("id")
        if lic not in permitted or not p.get("attribution") or not isinstance(photoid,int) or photoid in seen:continue
        sourceurl=p.get("url","")
        parts=urllib.parse.urlsplit(sourceurl)
        if parts.scheme!="https" or parts.hostname not in {"inaturalist-open-data.s3.amazonaws.com","static.inaturalist.org"}:continue
        if "/photos/"+str(photoid)+"/" not in parts.path:continue
        seen.add(photoid)
        medium=sourceurl.replace("/square.","/medium.").replace("/thumb.","/medium.").replace("/small.","/medium.")
        if "/medium." not in medium:continue
        record["candidates"].append({
          "photoId":photoid,"observationId":obs.get("id"),"taxon":name,
          "observer":(obs.get("user") or {}).get("login"),
          "photoAttribution":p["attribution"],"photoLicense":lic,
          "photoPageUrl":"https://www.inaturalist.org/photos/"+str(photoid),
          "observationUrl":"https://www.inaturalist.org/observations/"+str(obs.get("id")),
          "mediumUrl":medium,
          "apiTaxon":(obs.get("taxon") or {}).get("name"),
          "reviewState":"photo_license_found_visual_taxon_review_pending"})
        if len(record["candidates"])>=3:break
      if len(record["candidates"])>=3:break
  except Exception as e:record["error"]=str(e)
  data.append(record)
  print(k+1,name,"photos",len(record["candidates"]),flush=True)
result={"timestamp":"2026-10-28","scope":"34 RC7A draft identities","results":data,
 "licenseRule":"Check each photo, separate from observation license; no media packaged at audit stage"}
Path("rc7_inat_licensed_candidates.json").write_text(json.dumps(result,indent=2))
print("Candidates",sum(len(x["candidates"]) for x in data))
