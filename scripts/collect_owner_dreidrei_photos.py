#!/usr/bin/env python3
"""Private LIKAS iNaturalist photo candidate review for owner dreidrei.

User-provided direct permission applies only to photographs owned by dreidrei.
This script retrieves public photographs without treating "all rights reserved"
as CC. Images are candidates, not distributable until visual/rights review.
"""
import argparse,collections,hashlib,io,json,re,time,urllib.parse,urllib.request
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
PRIORITIES=ROOT/"app/src/main/assets/rc7_priority_batches_v1.json"
PHOTO_HOSTS={"inaturalist-open-data.s3.amazonaws.com","static.inaturalist.org"}
EXPECTED_OWNER_CREDITS={"andrei von tirona","dreidrei"}
API="https://api.inaturalist.org/v1/observations"

def candidate_photo(observation,photo,target_names):
    user=(observation.get("user") or {})
    taxon=(observation.get("taxon") or {})
    if str(user.get("login","")).casefold()!="dreidrei":return None
    if taxon.get("rank")!="species" or taxon.get("name") not in target_names:return None
    observation_id=observation.get("id");photo_id=photo.get("id")
    if not isinstance(observation_id,int) or not isinstance(photo_id,int):return None
    attr=str(photo.get("attribution") or "").strip()
    normalized=attr.casefold()
    # The owner asserted ownership, but only their own credited image qualifies.
    if not any(phrase in normalized for phrase in EXPECTED_OWNER_CREDITS):return None
    url=photo.get("url") or ""
    parsed=urllib.parse.urlsplit(url)
    if parsed.scheme!="https" or parsed.hostname not in PHOTO_HOSTS:return None
    match=re.fullmatch(r"/photos/(\d+)/(?:square|thumb|small|medium|large|original)\.(jpg|jpeg)",parsed.path,re.I)
    if not match or int(match.group(1))!=photo_id:return None
    newpath=parsed.path[:parsed.path.rindex("/")+1]+"medium.jpg"
    return {
        "taxon":taxon["name"],"photoId":photo_id,"observationId":observation_id,
        "observer":"dreidrei","photoAttribution":attr,
        "originalPhotoLicense":photo.get("license_code") or "all_rights_reserved",
        "rightsBasis":"owner_direct_private_research_permission",
        "permissionFile":"docs/LIKAS_DREIDREI_PRIVATE_PHOTO_PERMISSION_2026_10_28.md",
        "photoPageUrl":"https://www.inaturalist.org/photos/"+str(photo_id),
        "observationUrl":"https://www.inaturalist.org/observations/"+str(observation_id),
        "mediumUrl":urllib.parse.urlunsplit((parsed.scheme,parsed.netloc,newpath,"","")),
        "observationGrade":observation.get("quality_grade"),
        "reviewStatus":"candidate_photo_visual_review_pending",
    }

def collect(max_pages=40,page_size=30):
    plan=json.loads(PRIORITIES.read_text(encoding="utf-8"))
    targets={a["scientificName"] for b in plan["batches"] for a in b["entries"]}
    seen=set();matches=[]
    for page in range(1,max_pages+1):
        if page>1:time.sleep(.65)
        args={"user_id":"dreidrei","photos":"true","per_page":page_size,
              "page":page,"order":"desc","order_by":"created_at"}
        req=urllib.request.Request(API+"?"+urllib.parse.urlencode(args),
            headers={"User-Agent":"LIKAS-private-research-owner-photo-review/1.0",
                     "Accept":"application/json"})
        with urllib.request.urlopen(req,timeout=30) as response:
            payload=response.read(9000000)
        result=json.loads(payload)
        observations=result.get("results",[])
        for obs in observations:
            for p in obs.get("photos") or []:
                c=candidate_photo(obs,p,targets)
                if c is not None and c["photoId"] not in seen:
                    matches.append(c);seen.add(c["photoId"])
        print("Reviewed page",page,"observations",len(observations),
              "matched photos",len(matches),flush=True)
        if len(observations)<page_size:break
    return matches

def make_review(rows,limit=32):
    from PIL import Image,ImageOps,ImageDraw
    out=Path("rc7_owner_review");out.mkdir(exist_ok=True)
    selected=[];cards=[];taxa=set()
    for candidate in rows:
        taxon=candidate["taxon"]
        if taxon in taxa or len(selected)>=limit:continue
        try:
            req=urllib.request.Request(candidate["mediumUrl"],headers={
                "User-Agent":"LIKAS-private-research-owner-photo-review/1.0"})
            with urllib.request.urlopen(req,timeout=25) as response:raw=response.read(900001)
            if not 1000<len(raw)<=900000 or not raw.startswith(b"\xff\xd8\xff") or not raw.endswith(b"\xff\xd9"):
                continue
            image=Image.open(io.BytesIO(raw));image.load()
            if image.width<160 or image.height<160:continue
            card=Image.new("RGB",(380,320),"white")
            thumb=ImageOps.contain(image,(362,245))
            card.paste(thumb,((380-thumb.width)//2,0))
            draw=ImageDraw.Draw(card)
            draw.text((7,248),taxon[:40],fill="black")
            draw.text((7,268),str(candidate["photoAttribution"])[:47],fill="black")
            draw.text((7,286),str(candidate["photoId"])+" | "+str(candidate["originalPhotoLicense"]),fill="black")
            cards.append(card)
            file_name="inat_"+str(candidate["photoId"])+".jpg"
            (out/file_name).write_bytes(raw)
            selected.append(dict(candidate,sha256=hashlib.sha256(raw).hexdigest(),fileName=file_name))
            taxa.add(taxon)
            print("Previewed",taxon,candidate["photoId"],flush=True)
        except Exception as e:
            print("Candidate preview unavailable",taxon,str(e)[:130],flush=True)
    for start in range(0,len(cards),12):
        page=Image.new("RGB",(1140,1280),"#ececec")
        for i,c in enumerate(cards[start:start+12]):page.paste(c,((i%3)*380,(i//3)*320))
        page.save(out/("owner_contact_%02d.jpg"%(start//12+1)),quality=85)
    (out/"index.json").write_text(json.dumps({
        "grantScope":"private_noncommercial_research_only",
        "reviewStatus":"manual_appearance_rights_review_pending",
        "items":selected},indent=2))
    return len(selected)

def main():
    p=argparse.ArgumentParser()
    p.add_argument("--pages",type=int,default=40)
    p.add_argument("--no-photos",action="store_true")
    args=p.parse_args()
    if args.pages<1 or args.pages>48: p.error("Pages must be 1..48")
    candidates=collect(args.pages)
    Path("rc7_owner_photo_candidates.json").write_text(json.dumps({
        "ownerAccount":"dreidrei","status":"permission_claimed_visual_review_pending",
        "matched":len(candidates),"items":candidates},indent=2))
    print("Matched source-authored images",len(candidates),flush=True)
    if not args.no_photos: print("Review sheet photos",make_review(candidates),flush=True)
if __name__=="__main__":main()
