# APO RC112 | Compact screen labels and smaller launcher: source QA

**Date:** 10 November 2026, Asia/Manila
**Branch:** `feature/apo-rc110-highres-offline-photo-galleries-v170` (draft PR #115)
**Base:** signed RC111 code171 at `bebc0f665a61f36f10c8e2e6e598b76821e9b4e0`
**New source:** versionCode172, `0.27.3-rc112-minimal-ui-compact-icon`
**Status:** Source changes pushed; signed and emulator QA pending until successful exact-head CI and direct APK extraction.

The approved original 1254px APO icon master and historical master are unchanged. The build script downsizes only the master-derived transparent adaptive foreground to 310×310 inside 432×432, centered. No enlarging, redrawing or splash change. This is intended to leave visible horizontal margins in phone launchers; on-device crop acceptance is pending.

Atlas result cards omit `Book rarity` and `IUCN global` headings and present the category value directly. Reader badges use the same concise values, but full source distinctions and explanation dialogs remain accessible. Destructive Saved collection clearing is separated into Manage. Home/BioTools remove redundant destination subtitles and section introductions; PHMaps/BioDiscover are signposted as upcoming rather than promoted as completed tools. Species picker info becomes compact while explicit Select remains. GPS, History, Settings, and daily card text are shortened.

**Scientific and data boundaries:** IUCN assessment is not Philippine DENR, and Discovery Rarity remains editorial. Original Book 01 narratives, source occurrence records, all 333 gallery source labels, SQLite29, Android application ID, signing key, export format, and survey Complete/Incomplete/Not Counted behavior remain unchanged.

**Checks:** New `scripts/test_rc112_compact_interface.py`; inherited code171 test assertions shifted to code172; native workflows updated for code172 signed artifact/upgrade. Verify exact CI status, screenshots at 360dp standard and 320dp 200% system text, original certificate, package identity, APK ZIP integrity, retained gallery assets, real-device icon size, process restore, and survey data preservation. A passing source build does not count as field or accessibility acceptance.

**Outstanding work from the broader UX review:** App-wide dark mode and screen palette harmonization, asynchronous photo decoding and low-memory optimization, complete duplicated catalog/search unification, real-world GNSS and camera/audio checks, and fully verified 715-account bibliographic crosswalk. These are not claimed implemented here.
