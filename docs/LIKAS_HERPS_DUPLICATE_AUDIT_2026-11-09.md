# Reptiles and Amphibians photo duplicate audit
Date: 2026-11-09 (Asia/Manila)

## Scope and evidence
- Google Drive folder: [Reptiles and Amphibians](https://drive.google.com/drive/folders/1b1v9IiIuAYomcb7BAkv1YOi0LQAaBdEC).
- **184 photographs** in the folder before audit. Downloaded and processed all 184, with additional local images included in the comparison corpus (229 photographic images total).
- Duplicate screening: complete-file SHA-256, resized perceptual hashes and dHashes, SIFT image-feature correspondence on unflipped and horizontally flipped images, and direct visual inspection of the strongest and ambiguous pairs. The image files' reported labels are not validated species determinations.
- No exact byte-identical duplicates were detected in the 229-image local comparison corpus. Two distinct-file, same-photograph cases were visually confirmed in the target folder.

## Confirmed duplicate 1: skink scene, different crop
- **Keep in Herps:** `S. acutus.JPG`, Drive ID `1bEnrxH_n6C4nELbYNk9E1qSdMWoxgvto`, 6000 × 4000 px, 7,824,736 bytes.
- **Deleted from Herps:** `fig13a.JPG`, Drive ID `1dyA3WXpxlk1HMy5xXxKeKw3Ch5gvEp7r`, 4537 × 3403 px, 2,160,291 bytes. The figure shows the same photograph with different framing and size.
- **Archive copy created before deletion:** Drive ID `1aR7MdnGzNYFpidKf535fKodtEZmaomr6`, same original filename and 2,160,291 bytes.

## Confirmed duplicate 2: gecko scene, mirrored derivative
- **Keep in Herps:** `Psuedogekko pungkaypinit.JPG` (filename preserved as provided, not a taxonomic validation), Drive ID `1Fc2glhtkDxkbj4jR7k05AH3z2qV9eo2w`, 3670 × 2936 px, 9,733,432 bytes.
- **Deleted from Herps:** `fig11b.JPG`, Drive ID `1SJC-xk1TMyjJ6jZVxhiwNMF-432XYbRm`, 3596 × 2697 px, 2,611,558 bytes. The figure represents a horizontally mirrored/framed version of the same photograph.
- **Archive copy created before deletion:** Drive ID `1_BHeI2Y2BdSveCOw4o6cFJtl3YN5JGC8`, same original filename and 2,611,558 bytes.

## Result and limits
- Both permanent delete operations returned success, after archive creation and size readback. A subsequent Drive folder listing confirmed **182** images in Reptiles and Amphibians; both deleted filenames absent; both keeper files present. Archive listing confirmed both archived files present.
- Reversible preservation: [Duplicate photo archive (Reptiles and Amphibians)](https://drive.google.com/drive/folders/1q1xsR_Wqnv9nKIZf51YLEI9mHTIUd_19), a sibling folder under WILDLIFE PHOTOS. Archive copies hold the removed bytes although the original two categorized-file IDs were permanently deleted.
- Similar species, shared habitat backgrounds, or adjacent camera frames were not treated as duplicates without sufficient matching evidence. Some visually transformed or severely cropped duplicates could escape this screening.
- The two deleted filenames were not returned by top-level original-photo listing before archival. Following archive-folder creation, the root folder listing became inconsistent (returned only the new archive folder), although a direct metadata lookup for a known original file still showed that root as its parent. Therefore no claim of a fresh exhaustive inventory of the top-level source originals is made.
- No app code, embedded image assets, photo ownership assertions, or species identifications changed.
