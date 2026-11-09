# RC105 PHLibrary photo hero | Owner-supplied Mindanao landscapes

**Date:** 2026-11-09. **Source:** Four original JPEG images directly supplied in the owner conversation. They are used as *decorative* scenery in the PHLibrary Home hero, not as biological specimen documentation or evidence of species habitat occupancy. **Implementation:** draft PR #110, branch `feature/apo-rc105-phlibrary-clean-atlas-v165`.

| Owner file | Original bytes | Original SHA-256 |
| --- | ---: | --- |
| `_DSC0246-Enhanced-NR.JPG` | 757,613 | `5717a146fe1ed678496fa3d2e4199a7f6f58743e8bc8a4d89de43cee7ab55c78` |
| `_DSC0268-Enhanced-NR.JPG` | 471,117 | `5f33ca0e95797d6fcafa95a6d2aca6a6219558e68fdd1145eadb7c7fbcf824a6` |
| `_DSC8007-Enhanced-NR.JPG no watermark.JPG` | 621,906 | `3640b0bdf6198c0540273d41d4744e1b0bb7032f3723a3f9af44c3d1e2b332a9` |
| `landscape shot of karst forest.JPG` | 1,012,882 | `45a50d4acd7af8327d3c3e060e154f829eb0fbd2f97bacd78b1bda173cba6c88` |

Original JPEGs are retained unchanged in the owner-provided conversation archive `PHLibrary_RC105_Mindanao_Forest_Photos_HQ.zip` (not independently stored in the public repository). Some source photographs show a copyright credit, **© Andrei Von Tirona**. Use for this owner-directed app draft is accepted; public image redistribution/licensing beyond this preview has not been independently cleared.

**App derivative:** Four images cropped to landscape frames and packed vertically as one 1080 × 2432 WebP sheet, with four 1080 × 608 frames, quality 70. Actual WebP SHA-256 `b24352b109ae83d9baa41fda9a7363a8403f57a7bfe4ce41dbc1d981a208666f` and byte size **285,824**. Because the connected GitHub Contents API accepts UTF-8 text, the WebP bytes are transported as Base64 text in `app/src/main/assets/phlibrary_mindanao_forest_strip.webp.b64` (381,100 characters). The Android reader decodes them offline with `android.util.Base64InputStream` and `BitmapFactory`. Its image layer crops the frame to screen proportions, applies a text-legibility overlay, and uses a 950 ms crossfade at approximately 10-minute wall-clock boundaries. The upload was verified byte-for-byte against the local Base64 source. The app retains its previous illustrated forest as an explicit fallback only for missing/corrupt picture assets.

**Validation state:** Source upload and byte identity verified. CI Android emulator rendering, physical-display quality, reduced-motion acceptance and signed data-preserving upgrade must be recorded separately. A correct image asset does not prove a signed APK contains or renders the images until built and inspected.

This is PHLibrary Home art only. It does not modify the APO splash, official icon, scientific 715-account Book 01 v1.82 pack, rarity v0.3.4, signed package identity, or field database.
