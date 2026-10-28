#!/usr/bin/env python3
"""Public iNaturalist API metadata diagnostic. No image download."""
import json,urllib.parse,urllib.request,collections
base="https://api.inaturalist.org/v1/observations"
qs=[
 {"user_id":"dreidrei","per_page":"10","photos":"true","page":"1"},
 {"place_id":"6878","per_page":"10","photos":"true","photo_license":"cc-by,cc-by-nc,cc-by-sa,cc0","quality_grade":"research"}
]
summary=[]
for query in qs:
 url=base+"?"+urllib.parse.urlencode(query)
 try:
  req=urllib.request.Request(url,headers={"User-Agent":"LIKAS-scientific-photo-metadata-audit/0.28","Accept":"application/json"})
  with urllib.request.urlopen(req,timeout=28) as r:data=json.load(r)
  observations=data.get("results",[])
  photos=[p for o in observations for p in o.get("photos",[])]
  summary.append({"params":query,"total_results":data.get("total_results"),"returned":len(observations),
    "login_samples":list(dict.fromkeys((o.get("user") or {}).get("login","") for o in observations))[:4],
    "grade_counts":dict(collections.Counter(o.get("quality_grade") for o in observations)),
    "photo_host_counts":dict(collections.Counter(urllib.parse.urlsplit(str(p.get("url") or "")).hostname for p in photos)),
    "photo_license_counts":dict(collections.Counter(str(p.get("license_code")) for p in photos)),
    "sample":[{"observation_id":o.get("id"),"taxon":(o.get("taxon") or {}).get("name"),"grade":o.get("quality_grade"),
               "user":(o.get("user") or {}).get("login"),"photos":[{"id":p.get("id"),"url":p.get("url"),"license":p.get("license_code"),"attribution":p.get("attribution")} for p in o.get("photos",[])[:2]]}
               for o in observations[:6]]})
 except Exception as e:summary.append({"params":query,"error":str(e)})
open("rc7_photo_api_diagnostic.json","w").write(json.dumps(summary,indent=2))
print(json.dumps([{k:v for k,v in q.items() if k!="sample"} for q in summary],indent=2))
