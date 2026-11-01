# LIKAS RC22 | Original diagnosis, acoustic identification and Philippine habitat field studies

**Date:** 29 October 2026 (Asia/Manila).  
**Working branch:** `feature/likas-core-fieldwork-post-rc4`, draft PR #38.  
**Target preview:** `0.27.3-rc22-original-studies-preview`, versionCode **76**.  
**Science and privacy controls:** app ID `org.phwildlife.survey.beta08`, SQLite 27, existing survey data, original offline photographic licenses and separate startup work unchanged.

## Q1 | Literature-supported identification entries

Of the 12 original open identification sections, **five** receive narrowly scoped species-level evidence:

1. *Fejervarya vittigera*: Sanguila et al. (2026), ZooKeys 1272:203–251, DOI `10.3897/zookeys.1272.167304`, treatment heading **Identification**. The Agusan Marsh series describes an olive-grey back, variable vertebral line, yellow-marked groin/hind limbs, and absence of an outer tarsal/toe flap relative to *F. moodiei*. This is a voucher-linked character combination, not a national diagnosis from photographs alone.
2. *Trimeresurus schultzei*: Leviton et al. (2019), *Illustrated Key to the Snakes of the Philippines*, **Trimeresurus key couplet 1a**. Contrasting living green body and red tail are part of the key. It also uses hemipenial morphology, which is unsuitable for non-specialist field handling. Venomous snake safety remains explicit. Treat this as a character and key direction, not a colour-only identification verdict.
3. *Gekko gigante*: Rösler et al. (2006), *Salamandra* 42:197–211, **Comparisons**. The article summarizes *G. gigante* versus *G. ernstkelleri* with documented size, femoral-pore and dorsal-tubercle counts, citing Brown and Alcala 1978 and Alcala 1986. Comparative published diagnosis, not new morphological measurement in the 2006 paper.
4. *Sanguirana everetti*: Brown et al. (2017), *Herpetological Monographs* 31:182–203, **Table 3**. Selected measured *S. everetti* male SVL and graded flank colouring are drawn from the revision. Historical Visayan *everetti* populations were reallocated to *S. acai*. The label explains that size and colour alone do not resolve all stream frogs.
5. *Platymantis spelaeus*: Brown et al., *Silliman Journal* 43(1):91–109 (2002), original advertisement-call study summarized in the DOST STII index. The paired two-note calls and harmonic/amplitude structure provide acoustic identification context, not an automatic recorder classifier or a species-wide frequency limit.

**Unresolved seven**: *Platymantis hazelae*, *Platymantis isarog*, *Platymantis negrosensis*, *Platymantis banahao*, *Pelophryne albotaeniata*, *Limnonectes acanthi*, and *Dicaeum kampalili*. Published names, type localities, genus-level traits, variable stripes and generalized ecologies are not substituted for source-checked species diagnoses.

## Q2 | Original Philippine field habitat and ecology evidence

Study-specific context is central. Historical detections are never promoted to current national occupancy.

- **Agusan Marsh frog**: Sanguila et al. (2026), vouchers of *F. vittigera* beside irrigation channels and still pools of creeks and rivers, **Remarks**. The source does not support every Philippine rice field as occupied habitat.
- **Negros Bleeding-heart, *Gallicolumba keayi***: Mynott, Abrahams and Kerhoas (2021, online 2020), *Bird Conservation International* 31:640–655, DOI `10.1017/S0959270920000611`. Observations from 2018 in Northwest Panay measured canopy and understorey, with positive GLM associations (P = .003 and .001), 31 detections across the wider survey and three camera events at one site during 161 camera-days. Preserve detection effort and model limitations, avoid converting detections into bird counts or density.
- **Philippine crocodile, *Crocodylus mindorensis***: Brown et al. (2013), northern Sierra Madre herpetofaunal study in *ZooKeys* 266. Historic field observations in specific Isabela municipalities, including freshwater and certain coastal saline riverine habitats. Source-linked **Habitat** and **Distribution** notes exist only in the species reference account, not the live province-occurrence filter. Do not treat these 2013 historical observations as current local inventory.
- **Visayan spotted deer, *Rusa alfredi***: 2012 North Negros Natural Park camera-trap research published in *Oryx* (2013): 20 cameras, 185 camera-trap days. The new account statement identifies a past forest-interior survey detection, not a wild population census or a current record at an arbitrary station.
- **Sierra Madre forest monitor, *Varanus bitatawa***: Welton et al. (2010), DOI `10.1098/rsbl.2010.0119` original species description, specifies a frugivorous forest monitor. Feeding guild in the original paper is not a measured percent-fruit diet.
- **Negros cave frog, *P. spelaeus***: A published 2010 species comparison of *P. biak* includes a specific observer account of *P. spelaeus* direct-developing eggs in limestone crevices. This is an attributable life-history observation, not an inferred universal nest site.

## Corpus and QA gates

- Prior: 96 accounts, 413 cited biological facts, 281 per-species source links, 12 empty Identification entries, 46 Habitat, 24 Distribution, 73 Natural history and 70 Conservation.
- RC22 source: **96**, **426**, **290**, with empty Identification **7**, Habitat **46**, Distribution **23**, Natural history **71**, Conservation **70**. There are **13** added source-attributed facts and **nine** newly indexed taxon-source links.
- Exact source, URI, publication year, source location, source type and study scope stay in `rc22_original_diagnoses_habitat_field_studies_v1.json` and in each actual rendered unified account.
- `scripts/test_rc22_original_studies.py` checks every new fact in both corpus and exact taxon bibliography; checks unfilled seven, 96 unchanged account identities and preserved Android application identity/database version. Historical test totals updated to current corpus counts.
- Scientific Core QA must build the signed code76 preview, then Android emulator must demonstrate 14 instrumented tests, all-96 rendered screenshots, 200%-font End Survey button visible, and byte-identical old SQLite through install-upgrade.
- Real handset, field audio, large-font TalkBack beyond the recorder, long-duration sessions, and any production owner data upgrade remain unverified. Startup art changes are not part of RC22.

## Original literature and access boundaries

Relevant original URLs are stored in the versioned JSON. DOI links to journal research are cited instead of copying entire copyrighted descriptions or photographs. The acoustic call treatment is derived from a publicly retrievable scientific abstract, not examined raw sound recordings. Published comparative keys may use characters that observers should not obtain by handling sensitive or venomous species. Mixed source types remain explicitly labeled.

**Immediate next step:** Verify the latest code76 signed APK and current emulator run independently, attach the binary and QA ZIP directly to this chat, inspect the 200%-font recorder capture, then continue original species descriptions for the seven unresolved taxa without inventing a diagnosis.

## Final code76 Android acceptance evidence, 29 October 2026

- Scientific Core QA passed (including runs 40813434462 and 40813547425); signed RC22 APK verified at 9,573,299 bytes, SHA256 63afa9629c53f4bbd4abd88d49a130301261f61c305166849635f206e6d78430. APK package, versionCode76, code76 versionName and original v2 signer fingerprint match prior signed builds.
- Actual Android 14 emulator workflow **40813385969** completed success, with artifact **12871930730**, ZIP SHA256 c79d090d06823156c00197e2797bb3f7fe1282a936a1f7e80e18373848cdffd1. Extracted instrumented log ends **OK (14 tests)**; markers assert 96 species render and recorder dock.
- The 96 JPEG first-viewport images are individually distinct, 24 per taxonomic group; all 96 CSV-reported image byte lengths match their JPG files. Manually inspected four 24-panel contact sheets. No obvious major heading/card control overlap observed in these first viewports. Full scrolling/accessibility still needs hands-on QA.
- Recorder screenshot at 200% system font shows complete Save, Review and End buttons within 480×960 frame. Original pre-upgrade and after-upgrade SQLite fixture bytes match exactly (1,613,824 bytes, SHA256 0c43257f28782b6a45794bb9a6e91b55c655066bd7cf3734c6fb878af839b2a8). Media fixture checks pass via existing emulator script.
- **Acceptance boundary:** This validates the RC22 emulator and source build, not any real Android phone or owner records. PR #38 remains draft, startup assets isolated. Seven unsupported identification sections remain open for source-specific verification. Direct APK and emulator QA ZIP attached in the originating chat only.
