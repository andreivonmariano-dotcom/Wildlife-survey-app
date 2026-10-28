#!/usr/bin/env python3
"""Review iNaturalist photo candidates, then package only hash-pinned approvals.

Default: inspect local lock with no network. --audit uses the public iNat API to
propose photos from a named observer; it never approves or packages proposals.
--install-approved verifies each pre-reviewed JPEG SHA256 before bundling.
"""
import argparse
import base64
import hashlib
import json
import re
import time
import zlib
import tempfile
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
PRIORITY=ROOT/"app/src/main/assets/rc7a_field_cards_v1.json"
LOCK=ROOT/"docs/RC7_INAT_APPROVED_LOCK_V1.json"
MANIFEST=ROOT/"app/src/main/assets/offline_inaturalist_manifest_v1.json"
PHOTO_DIR=ROOT/"app/src/main/assets/offline_photos"
PRIVATE_BUNDLE=ROOT/"app/src/main/assets/likas_ph_checklists_v2026_private.b64"
PRIVATE_SHA256="7845ceadc4b6d6226781478c72281364c876a4c1ad47b80d21b7d176ab7dc4c6"
API="https://api.inaturalist.org/v1/observations"
LICENSES={"cc0","cc-by","cc-by-sa","cc-by-nc","cc-by-nc-sa"}
LICENSE_URL={
 "cc0":"https://creativecommons.org/publicdomain/zero/1.0/",
 "cc-by":"https://creativecommons.org/licenses/by/4.0/",
 "cc-by-sa":"https://creativecommons.org/licenses/by-sa/4.0/",
 "cc-by-nc":"https://creativecommons.org/licenses/by-nc/4.0/",
 "cc-by-nc-sa":"https://creativecommons.org/licenses/by-nc-sa/4.0/",
}
MAX_BYTES=900000
MAX_ITEMS=60
IMAGE_HOSTS={"inaturalist-open-data.s3.amazonaws.com"}
NAME_RE=re.compile(r"^[A-Z][A-Za-z-]+ [a-z][a-z-]+(?: [a-z-]+)?$")

def jsonfile(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))

def candidates_from_results(results,names,observer="dreidrei"):
    """Photo license governs reproduction, independent of observation license."""
    found=[]
    for observation in results:
        if not isinstance(observation,dict): continue
        user=observation.get("user") or {}
        if str(user.get("login","")).casefold()!=observer.casefold():continue
        taxon=observation.get("taxon") or {}
        scientific=taxon.get("name","")
        if (taxon.get("rank")!="species" or scientific not in names
                or observation.get("quality_grade")!="research"):continue
        number=observation.get("id")
        if not isinstance(number,int) or number<=0:continue
        for photo in observation.get("photos") or []:
            code=str(photo.get("license_code") or "").lower().strip()
            url=str(photo.get("url") or "")
            attr=str(photo.get("attribution") or "").strip()
            photoid=photo.get("id")
            if code not in LICENSES or not attr or not isinstance(photoid,int) or photoid<=0:
                continue
            parsed=urllib.parse.urlparse(url)
            if parsed.scheme!="https" or parsed.hostname not in IMAGE_HOSTS:continue
            if not re.fullmatch(r"/photos/\d+/(square|thumb|small|medium|large|original)\.(jpg|jpeg)",
                    parsed.path,re.I):continue
            filename=parsed.path.split("/")[-1]
            basename=filename.rsplit(".",1)[0]
            medium=parsed._replace(path=parsed.path.replace("/"+basename+".","/medium.")).geturl()
            found.append({
                "photoId":photoid,"observationId":number,"taxon":scientific,
                "observer":observer,"photoAttribution":attr,"photoLicense":code,
                "photoLicenseUrl":LICENSE_URL[code],
                "observationUrl":"https://www.inaturalist.org/observations/"+str(number),
                "photoPageUrl":"https://www.inaturalist.org/photos/"+str(photoid),
                "mediumUrl":medium,
                "approvalState":"needs_independent_photo_and_taxon_review",
            })
    return found

def all_philippine_names():
    """Keep source-list membership separate from observed site presence."""
    raw=base64.b64decode(PRIVATE_BUNDLE.read_text().strip(),validate=True)
    if hashlib.sha256(raw).hexdigest()!=PRIVATE_SHA256:
        raise ValueError("Private WBCP/Sy checklist fingerprint mismatch")
    rows,_=json.loads(zlib.decompress(raw))
    all_names={str(row[3]) for row in rows if len(row)>3}
    for batch in jsonfile(ROOT/"app/src/main/assets/rc7_priority_batches_v1.json")["batches"]:
        all_names.update(r["scientificName"] for r in batch["entries"])
    return all_names

def query_observer(observer,names,limit_pages):
    out=[]
    for page in range(1,limit_pages+1):
        if page>1:time.sleep(1.0)
        params={"user_id":observer,"photos":"true","per_page":30,"page":page,
                "quality_grade":"research","order_by":"created_at","order":"desc"}
        url=API+"?"+urllib.parse.urlencode(params)
        req=urllib.request.Request(url,headers={
            "User-Agent":"LIKAS-private-research-photo-review/1.0 (contact via repository)",
            "Accept":"application/json"})
        with urllib.request.urlopen(req,timeout=20) as response:
            payload=response.read(9000000)
        result=json.loads(payload)
        group=result.get("results") or []
        out.extend(candidates_from_results(group,names,observer))
        if len(group)<30:break
    unique={}
    for candidate in out: unique.setdefault(candidate["photoId"],candidate)
    return list(unique.values())

def valid_approved(item,targets):
    required=("photoId","observationId","taxon","observer","photoAttribution",
        "photoLicense","photoLicenseUrl","mediumUrl","observationUrl","photoPageUrl","sha256",
        "reviewedBy","reviewedOn","approvalState")
    if any(not item.get(k) for k in required):return False
    if item["taxon"] not in targets:return False
    if item["photoLicense"] not in LICENSES:return False
    if item["photoLicenseUrl"]!=LICENSE_URL[item["photoLicense"]]:return False
    if item["approvalState"]!="approved_private_research":return False
    if not isinstance(item["photoId"],int) or item["photoId"]<=0:return False
    if not isinstance(item["observationId"],int) or item["observationId"]<=0:return False
    if not re.fullmatch(r"[0-9a-f]{64}",item["sha256"]):return False
    if not re.fullmatch(r"\d{4}-\d{2}-\d{2}",item["reviewedOn"]):return False
    url=urllib.parse.urlparse(item["mediumUrl"])
    if url.scheme!="https" or url.hostname not in IMAGE_HOSTS:return False
    if not re.fullmatch(r"/photos/\d+/medium\.(jpg|jpeg)",url.path,re.I):return False
    if not url.path.startswith("/photos/"+str(item["photoId"])+"/"):return False
    if item["observationUrl"]!="https://www.inaturalist.org/observations/"+str(item["observationId"]):
        return False
    if item["photoPageUrl"]!="https://www.inaturalist.org/photos/"+str(item["photoId"]):
        return False
    return True

def package_approved(items,targets,photo_dir=PHOTO_DIR,manifest_path=MANIFEST,
                     fetcher=None):
    """Verify complete immutable lock before writing any manifest or JPEG."""
    if fetcher is None:
        def fetcher(url):
            req=urllib.request.Request(url,headers={"User-Agent":"LIKAS-research/1.0"})
            with urllib.request.urlopen(req,timeout=20) as response:
                return response.read(MAX_BYTES+1)
    if len(items)>MAX_ITEMS:raise ValueError("Approved media count exceeds cap")
    ids=set()
    for item in items:
        if not valid_approved(item,targets):raise ValueError("Unapproved/invalid photo metadata")
        if item["photoId"] in ids:raise ValueError("Duplicate photo ID")
        ids.add(item["photoId"])
    # Read all bytes and compare immutable hashes before a single write.
    verified=[]
    for item in items:
        body=fetcher(item["mediumUrl"])
        if not isinstance(body,bytes) or len(body)>MAX_BYTES or len(body)<40:
            raise ValueError("Unexpected image payload size")
        if not body.startswith(b"\xff\xd8\xff") or not body.endswith(b"\xff\xd9"):
            raise ValueError("Source is not a complete JPEG")
        if hashlib.sha256(body).hexdigest()!=item["sha256"]:
            raise ValueError("iNaturalist photo bytes changed since review")
        verified.append((item,body))
    photo_dir=Path(photo_dir);manifest_path=Path(manifest_path)
    photo_dir.mkdir(parents=True,exist_ok=True)
    manifest=[]
    for item,body in verified:
        path="inat_"+str(item["photoId"])+".jpg"
        (photo_dir/path).write_bytes(body)
        manifest.append({
            "id":"inat_"+str(item["photoId"]),"category":"species",
            "taxon":item["taxon"],"fileName":path,
            "observer":item["observer"],"photoId":item["photoId"],
            "observationId":item["observationId"],
            "author":item["photoAttribution"],"license":item["photoLicense"].upper(),
            "licenseUrl":item["photoLicenseUrl"],"sourceUrl":item["photoPageUrl"],
            "observationUrl":item["observationUrl"],
            "offlineAsset":"offline_photos/"+path,
            "sha256":item["sha256"],
            "caption":"iNaturalist photographic identification reference. Not a LIKAS field sighting.",
            "edited":"Source medium JPEG; no content alteration",
        })
    manifest_path.parent.mkdir(parents=True,exist_ok=True)
    manifest_path.write_text(json.dumps({
        "schemaVersion":"1","edition":"2026-10-28",
        "preferredObserver":"dreidrei","usage":"private_noncommercial_research",
        "items":manifest,
    },indent=2)+"\n",encoding="utf-8")
    return len(manifest)

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--audit",action="store_true",
        help="Query observer, write candidates. This does not give photo approval.")
    parser.add_argument("--install-approved",action="store_true",
        help="Package only reviewed, exact SHA-256-pinned photographs")
    parser.add_argument("--observer",default="dreidrei")
    parser.add_argument("--pages",type=int,default=6)
    parser.add_argument("--output",type=Path,default=Path("rc7_inat_photo_review_candidates.json"))
    args=parser.parse_args()
    if args.pages<1 or args.pages>20:parser.error("pages must be 1–20")
    groups=jsonfile(PRIORITY)
    targets=all_philippine_names()
    if args.audit:
        candidates=query_observer(args.observer,targets,args.pages)
        args.output.write_text(json.dumps({
            "observer":args.observer,"licenseScreenedCount":len(candidates),
            "photoCandidates":candidates,
            "status":"review_required_before_packaging",
        },indent=2)+"\n",encoding="utf-8")
        print("Review candidates:",len(candidates),"; NO files approved")
    elif args.install_approved:
        lock=jsonfile(LOCK)
        print("Verified and installed photos:",package_approved(lock["items"],targets))
    else:
        lock=jsonfile(LOCK)
        print("Pinned photo approvals:",len(lock["items"]))
        print("Use --audit for proposal only; --install-approved only after review.")

if __name__=="__main__":
    main()
