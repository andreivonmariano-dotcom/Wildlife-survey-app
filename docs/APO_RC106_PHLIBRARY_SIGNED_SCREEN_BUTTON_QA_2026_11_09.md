# RC106 PHLibrary screen-and-button audit | Signed acceptance record

**Date:** 9 November 2026 (Asia/Manila). **Source:** branch `feature/apo-rc106-phlibrary-ux-polish-v166`, exact signed code HEAD `342bedc2b44111910e47d07c9657563385fd9a31`, draft PR #111 on RC105 PR #110. App versionCode **166**, versionName `0.27.3-rc106-phlibrary-polish`, package `org.phwildlife.survey.beta08`, unchanged SQLite schema29.

## Verified outcome

- **GitHub Actions run** `42560346632` concluded **SUCCESS**, including Python source/scientific tests, Android compilation, JVM tests, signed APK and immutable source asset checks, Android 14 emulator UI at normal 360 × 800 /100% font and narrow 320 × 720 /200% font, and signed RC92 code152 → RC106 code166 in-place no-clear-data upgrade.
- **Actual native views reviewed:** Home, Species Atlas, Species Profile, Habitat Atlas index, Habitat profile, full bibliography, and candidate citation results at each font scale (14 screenshots); inherited startup normal/reduced motion tests additionally supplied 4 PNGs. Emulator evidence artifact **13649267880**; ZIP recovered in conversation as `RC106_Final_PHLibrary_Native_Evidence.zip`. Native photo-height regression checks prove the Home forest hero does not expand to obscure the main navigation. Enlarged Species Atlas status pills are full-width and do not strand icons or long IUCN labels.
- **Upgrade evidence:** artifact **13648599060**. Both `pre_upgrade_seed.txt` and `post_upgrade_validation.txt` say `OK (1 test)`; `android_runtime_errors.txt` is **0 bytes**. This covers one baseline upgrade scenario and does not substitute for the owner's phone.
- **Signed preview artifact:** **13648429524**. APK filename `LIKAS_RC106_PHLibrary_Screen_Button_Polish_signed_code166.apk`, exactly **12,771,607 bytes**, SHA-256 **`1e2abc1a25c759db22c67a9e59a95e6f8dd9906f5f0655ef105948cc4e789f7c`**. Independent ZIP archive CRC verification and APK Signature Scheme V2 DER signer extraction confirm original signer certificate SHA-256 **`f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937`**.
- **Asset-level preservation:** Independently compared every `assets/` entry inside signed RC105 code165 and RC106 code166. Exactly **131 assets**, all identical names and SHA-256 bytes: canonical 715-account Book01 v1.82, 1,159 bibliography records, 15 source habitat profiles, 21 provisional species–habitat link edges, PAIC714 reference, WorldCover, APO art and all four owner Mindanao cover photos. No field data schema, source account narratives, rarity v0.3.4, scientific identities or canonical binary assets were modified.

## Button and layout findings addressed

Home has only Search and four destinations, with redundant wildlife-group Home shortcuts and headings removed. Forest background stays at fixed 194dp/240dp heights for 100%/200% text, rotates offline, respects reduced motion and cancels offscreen callbacks.

Species Atlas retains continuously scrollable ListView, group chips, Filter and Sort dialogs, family/genus/habitat-text/rarity/IUCN/saved filters. Removed duplicate title and reset; Clear is visible only for active state, resetting all query/filter/group/sort dimensions. On 200% text rarity/IUCN pills stack at full width. Empty state and result count are shorter.

Species Profile retains unchanged, selectable narrative, separate status-color badges, short Save/Saved and Citations controls, A−/A+ text adjustment, related family/previous/next navigation. Back traverses the species visited inside the reader before exiting; configuration change restores page and scroll history.

Habitat Atlas cards omit filler subtitles, profile field sections use shorter expandable headings, the index remembers its scroll position, and links retain actual traceable source titles/URLs. Evidence limits are on demand; unlinked profiles never imply surveyed absence. Bibliography opens with Back, Search and source rows rather than a lengthy preface; it preserves full selectable details and only calls possible author–year matches **Not verified**, not reconciled citations.

## Remaining boundaries

Draft PR #111 remains open, unmerged. No actual owner-phone install, TalkBack audit, low-end memory/performance measurement, exhaustive filter combinations, long-duration ten-minute photo-rotation test, public image-redistribution review, source-reviewed citation join, or taxon-concept bookmark migration has been completed. These remain future acceptance work, not a reason to change validated science. Do not call this a stable release.

The signed APK, 14-screen contact sheet and evidence ZIP are present in the current conversation as verified direct sandbox downloads. New chat sessions must reverify exact paths before linking.
