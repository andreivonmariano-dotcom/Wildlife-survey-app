# APO | Owner-approved official mountain icon, hard lock

**Decision:** 2 November 2026 (Asia/Manila)  
**State:** **APPROVED, HARD-LOCKED as the definitive visual identity.** Design approval, not yet evidence that launcher assets were replaced in a signed APK.

## Product name and role

The intended public-facing app identity is **Apo** (APO in a wordmark), referencing Mount Apo. The app is an all-in-one knowledge and field tool for biologists. Scope includes field survey workflows, species/ecology learning references, habitat information, research, analysis, and future modules. Do not use a subtitle that reduces it to "fieldwork only." The icon contains **no text**. User asked explicitly for a new name and for a textless mountain icon. Display-label migration is planned as a separate audited change; preserve Android applicationId `org.phwildlife.survey.beta08`, signing cert and historical storage identity, even after brand rename.

## Approved master, exact image fingerprint

- **Approved image:** `a_clean_modern_minimalist_logo_app_icon_on_a_rou.png` from the owner's 2026-11-02 conversation; the precise approved generated image is held in that conversation.
- **Source format and dimensions:** PNG, RGB, 1254 × 1254 pixels.
- **Exact SHA-256:** `eae6582c90ff8bbe394a795763aacb68da40ca9a81acc79ec587b2c227e9c284` (verified against the local supplied source). This is the approval identity. Do not replace with a similar but different image.
- **Motif:** single deliberate, swooping inverted-V mountain ribbon with a dominant left peak, smaller right summit and folded inner jade-green depth.
- **Color and canvas:** warm ivory highlight, muted jade-green interior and deep forest-green near-black rounded-square canvas with subtle tonal depth.
- **Shape discipline:** crisp, purposeful, asymmetrical geometry, tapering to elegant fine points. Keep generous blank space and scale to launcher safe zone.
- **Excluded:** app-name text inside icon, frog, lily pad, decorative symbols, extra landscape layers, noisy texture, glowing particles, faux 3D bevels, generic mountain clip art, pixel art and new palettes.
- **Artwork medium:** the owner's approved master is a **raster render** of a vector-like emblem. A true vector adaptation is an engineering derivative, not automatically pixel-identical. A derivative must be compared visually to the master at launcher size, avoid cropping in Android adaptive masks, and preserve the approved silhouette, colors and layering.

This owner instruction **supersedes** older launcher frog icon locks (e.g. `docs/LIKAS_ICON_ASSET_LOCK_2026_10_30.md` and `docs/QA_v0_19_8_MINIMAL_FROG_ICON.md`) **for the app launcher identity only**. Older locked wildlife utility/navigation illustrations remain as historical source until replaced by a separately accepted unified minimalist UI. Do not silently rewrite those old records or their approved asset hashes.

## Implementation requirements and acceptance

- Install exact image-derived approved artwork under a new `apo_` resource identity; update normal and round Android adaptive icon foregrounds and `android:icon` metadata without changing app ID/signing identity.
- Update splash/app title/accessible icon descriptions consistently only as the new name and startup UX are implemented, without overwriting scientific/fieldwork data or package references.
- Keep the original PNG immutable, with SHA-based test and documented release hashes; optimized Android WebP/PNG derivatives must each have their *own* checksums and traceable conversion settings.
- Test 48dp home-screen rendering and Android round/squircle masks, contrast, 320dp displays, high-contrast/theme modes, 200% text elsewhere, and real-device installed upgrade. Avoid bundling the original white outer-background corners as visible white triangles inside any adaptive mask.
- Run the full source/Android tests and inspect the exact signed APK before describing the new identity as shipped. The current RC71 APK remains the last signed reference-packaging baseline until a new build explicitly proves the new icon is embedded.

## Proposed startup redesign (not yet approved or implemented)

Use a full-screen deep-forest-to-near-black static backdrop that matches the official mark. Center the mountain mark, then reveal the standalone `APO` wordmark (separate text layer, never printed inside the launcher artwork). Movement should be sparse and purposeful: a short trim-path mountain outline reveal (~300 ms), typography fade/translate (~220 ms), and immediate cross-fade to Home once references are ready. Avoid mandatory waiting, repeating particle/forest animations, dramatic zoom, and GPS/network requests at launch. Respect Android system animator setting and Less Motion. If reference initialization lasts longer than the opening, display truthful `Preparing offline references` status without pretending it completed. Home/resume must work after recovery and failure. No fabricated progress percentages; preserve explicit failure alerts and historical data.

**This document locks the approved icon image, not the unapproved startup animation sequence.**
