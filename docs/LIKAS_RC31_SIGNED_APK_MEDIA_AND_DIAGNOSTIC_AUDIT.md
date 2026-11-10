# RC31 | Packaged Offline Photo and Diagnostic Coverage

**Exact artifact:** `LIKAS-v0.27.3-RC31-priority-diagnostics-debug.apk`, Android versionCode 91, source `15b9e69d3d68325915d2212d0ee56cc9b8ca5f50`, scientific-core run `40841800337`.

The actual compiled APK ZIP was inspected, rather than assuming its original ten-item `offline_photo_manifest_v1.json` included all licensed imagery. The APK contains **58 JPGs** under `assets/offline_photos/`. The two photo manifests describe ten original records and **49** separately permitted iNaturalist reference photographs. The total is not necessarily the arithmetic sum of manifests, since individual entries and available paths must be verified against the ZIP.

Of the **96 priority taxa**, **36 have at least one actual packaged JPEG** (12 Birds, 10 Amphibians, 8 Reptiles and 6 Mammals). **60 have no packaged photographic reference**. The following are photo-present in this exact APK:

- *Acerodon jubatus*
- *Ansonia mcgregori*
- *Anthracoceros montani*
- *Barbourula busuangensis*
- *Buceros hydrocorax*
- *Ceyx argentatus*
- *Cuora philippinensis*
- *Cynocephalus volans*
- *Dicaeum haematostictum*
- *Erythropitta kochi*
- *Gallicolumba platenae*
- *Hydrosaurus pustulatus*
- *Leptobrachium lumadorum*
- *Leptobrachium mangyanorum*
- *Limnonectes magnus*
- *Manis culionensis*
- *Ophiophagus salvatana*
- *Pelochelys cantorii*
- *Pelophryne lighti*
- *Penelopides affinis*
- *Penelopides panini*
- *Pithecophaga jefferyi*
- *Platymantis paengi*
- *Platymantis rabori*
- *Platymantis spelaeus*
- *Pteropus pumilus*
- *Rhabdotorrhinus leucocephalus*
- *Rhabdotorrhinus waldeni*
- *Rusa marianna*
- *Sanguirana everetti*
- *Sarcophanops samarensis*
- *Siebenrockiella leytensis*
- *Sus cebifrons*
- *Trimeresurus mcgregori*
- *Varanus bitatawa*
- *Varanus palawanensis*

The 49 separately sourced photographs include 26 records of direct copyright-holder permission for **LIKAS private, noncommercial research**, 21 CC-BY-NC records, and two CC-BY records. Separate original photos include CC0, CC-BY and CC-BY-SA. These rights differ. This preview is not a grant to redistribute private-authorized media outside its explicit scope. Photographs alone do not establish type-specimen identification, age, sex, local occurrence, or completeness of the diagnostic account.

**RC31 diagnostic improvements:** original specimen-scale comparison for *Varanus rasmusseni*; original single-type-specimen comparison for *Crateromys paulus*; peer-reviewed sampled characters for *Kaloula picta*; confirmed replacement-name publication only for *Dicaeum kampalili*. Validated image-to-character comparison is still pending. No unlicensed image was introduced in RC31.

**Release QA:** Scientific-core CI succeeded. APK ZIP integrity, compiled manifest identity (package `org.phwildlife.survey.beta08`, versionCode 91), and pinned original APK v2 signer were checked. Emulator visual QA, media-attribution legibility, handset upgrade, GNSS, camera, recordings and owner-data restore remain untested.
