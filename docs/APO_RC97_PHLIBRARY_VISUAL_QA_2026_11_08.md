# APO RC97 | PHLibrary text-first visual overhaul and acceptance

**Date:** 8 November 2026, Asia/Manila. **Parent:** RC96 (which derives directly from owner-confirmed RC95). **Draft PR:** #102. **Android:** `org.phwildlife.survey.beta08`, code157, version `0.27.3-rc97-phlibrary-visual`, existing signer and SQLite29.

## Owner decision and scope

After viewing a real screenshot of the old icon-heavy PHLibrary hub, the owner directed an overhaul of PHLibrary layout/theme, then expressly directed **remove icons** and **remove photographs**. This applies to the PHLibrary home, Book 01 directory and book species reader. Legacy licensed media and other APO research/survey tools must not be deleted or accidentally modified.

- Home: replace multiple giant icon cards and duplicate checklist/guides/Book 01 entries with one typographic forest-green Book 01 cover plus short text-only reference rows. Photo comparison and photograph-led guide links removed from this hub. No generic substitute photos.
- Directory: searchable complete 714 species with name/family/order, group chips and Discovery Rarity filter, compact text-only list; native APO palette and inputs.
- Species profile: one preserved v1.75 paragraph, scientific name, family/order and plain-text status labels, small tappable source strip at bottom, text previous/next navigation, no photo plate or decorative rarity symbols. No source records changed.
- Accessibility: Back action written as text; 200% font gets shorter contextual labels and search hint and a wider Back control; long text can wrap, all content remains scrollable.

## Tested baseline and actual emulator evidence

One full RC97 source build (`44ad93c45c83458c7576d04bcd0ece9a6351b5f6`) passed GitHub workflow **42401750694**. 485 Python source tests, JVM build, signed Android app/test assembly, retained Android v2 certificate and package identity, real Android 14 emulator UI at **360dp/100%, 412dp/130%, 320dp/200%** and signed RC92→RC97 upgrade passed. Evidence artifact **13584969467**, signed APK artifact **13585524682**, upgrade artifact **13585823005**. This baseline is *superseded* by subsequent visual typography refinements below and must not be presented as the final later build without checking the source SHA.

Visual inspection of all 15 captured RC97 screenshots showed clean text-only hub and lists with complete Philippine Eagle one-paragraph account, the source strip at the bottom and no unexpected images. The 360dp and 412dp layouts have good hierarchy and legible controls. At 320dp/200% there were three optimization opportunities: (1) narrow text Back button, (2) long directory search hint, and (3) multiple repeated cover/profile headings pushing primary content down. These were then updated in `PhLibraryStyle`, `LikasLibraryActivity`, `TaxonDetailActivity`, `PhLibraryDirectoryActivity`, and `PhLibraryBook01`. Related Android tests updated.

## Remaining release gate

Build and Android-emulator retest the **final compact-font patch** after the successful first RC97 baseline. Verify PNG samples, original signed upgrade, 714 embedded accounts and package identity; only then extract exact latest APK and provide a direct `sandbox:/mnt/data` link in the chat. User's physical handset, historical real records, TalkBack and outdoor sunlight remain untested; emulator synthetic signed upgrade is not a physical-device test.
