# LIKAS RC34 | Conservation-first card system

**Owner decision:** Replace every *Priority species* display tier with independently sourced, dated IUCN global and DENR Philippine national categories. Rarity is a separate metric and remains unclassified without sufficient observation-effort evidence. **Date:** 29 October 2026.

## Visual hierarchy

The LIKAS Library opens **Species** and **Habitats** through one side-scrolling discovery screen, each with two fixed tabs. The old full searchable catalog and 15-item habitat directory remain one tap away. Every card uses a soft-edged ivory frame, forest/pond ink, a licensed offline photo where present, and a fallback original leaf or habitat glyph where absent. Species cards show common name, italic scientific name, taxon group, and independently sourced category + year. Cards open scientific accounts. Habitat cards use ecological-group coloring only and open a dedicated source-cited habitat profile.

**Category frame colors** (approximate sRGB, not a legal color code):

| Published category | Frame | Category label | Animation |
| --- | --- | --- | --- |
| CR | Clay red `#95463A` | IUCN CR or DENR CR with year | Single soft enter |
| EN | Burnt ochre `#B5703B` | IUCN EN or DENR EN with year | Single soft enter |
| VU | Aged gold `#AA8B4A` | IUCN VU or DENR VU with year | Single soft enter |
| NT, LC, DD | Neutral border | **Only** when dated and independently verified | Single soft enter |
| Not checked in LIKAS | Neutral border | **No category code** | Single soft enter |
| OTW, other national classes | Neutral until an original national source is individually verified | Explicit DENR label | Single soft enter |

**No universal rarity ranking.** Rarity means site- and period-specific encounter frequency or abundance backed by survey effort, sampling method, spatial scope, date, detection conditions and cited source. A short-range endemic is not automatically called rare. IUCN CR does not mean rare at any given site. Missing status does not mean IUCN LC, DD, NE or DENR Not Listed. Endemicity, research depth, number of photos and source-catalog membership do not control the border.

## Assessment model

Card payload: `app/src/main/assets/rc34_conservation_card_status_v1.json`. Six names have dated, reviewed display entries: three original IUCN category records and three separate DENR original-order entries. The remaining species have no status badge. Each record includes authority, category, year, edition, exact source URL and verification limitations. The 2020 Northern Rufous Hornbill IUCN category is only a secondary specialist summary and is **not** shown as original IUCN-verified. Its separately checked 2019 national classification remains visible. IUCN NT/LC are shown only where checked; an old conservation-study section does not constitute a dated red-list category.

Source-bound national order: [DENR DAO 2019-09](https://bmb.gov.ph/downloads/WRD/WC/WC2020/stat_and_lists_of_wildlife/fauna/dao-2019-09.pdf). This is a **2019 edition label**, not a promise that no later law has been published. IUCN documentation: [Red List categories](https://iucn.org/index.php/resources/conservation-tool/iucn-red-list-threatened-species).

## Card layout

- Header: brief kicker, large Species / Habitats wordmark, two controls.
- Species: **Conservation** shelf for currently individually verified entries; then group shelves for Birds, Amphibians, Reptiles and Mammals. Each is horizontally swipable. **All species** opens the complete searched checklist database.
- Species card: 212dp width, contained 116dp wildlife photo or decorative glyph, 16sp common name, 12sp italic binomial, compact dated source badges, group label. 200% font uses wider 226dp cards. Entire card is a ≥48dp touch surface. Photos retain established attribution and photographer permissions in the expanded account.
- Habitats: horizontal shelves for Terrestrial, Edaphic, Aquatic, Coastal and Human-modified. Group colors describe formations, **not** IUCN ecosystem collapse categories and **not** wildlife species threat. Profiles include original Field Signs, Water & Soils, Vegetation, Record, Limits, and source buttons. A habitat has no arbitrary conservation tier, species-occurrence prediction or fabricated elevation range.
- Motion: smooth native horizontal swipe and a short page fade once on entry. The existing **Less motion** setting suppresses optional animation. No repetitive shimmer, light flash or incessant movement, particularly for CR species.
- The same visual language should later be extended to survey setup, field forms, habitat checklist detail, species research details, and analytics without weakening functionality.

## Retained scientific data

The original RC18 file name and `prioritySpeciesCount=96` remain solely as **legacy internal identifiers** to avoid destructive migration and preserve the 505 sourced facts, full 96 long research accounts, cross-study identifiers and existing SQLite27 survey observations. UI removes the public `Priority species (96)` and other Priority filters from `Rc7SpeciesLibraryActivity`. This is not taxon data deletion.

## Engineering QA and open issues

- `LikasDiscoveryActivity`, `LikasHabitatProfileActivity`, `LikasLibraryActivity`, manifest and source filters have changes on the existing core branch. The new source test `scripts/test_rc34_conservation_discovery_ui.py` checks claims, six dated authority records, preservation, no priority filter and original photos.
- Core QA automated tests, Android assemble, signed APK, icon continuity, accessibility readout, 200% font and normal-font screenshot review, actual owner-data upgrade remain distinct gates. The acceptance state will be added after the CI run is checked.
- Remaining: verify every dated IUCN and DENR source for all 96 taxa, build a scientifically bounded local rarity dataset, refine screen-by-screen comparison aesthetics, and expand original/rights-cleared wildlife and habitat pictures. Do **not** invent these attributes in the meantime.

**Affected governing requirements:** G-01/G-03/G-05, L-02/L-03/L-04, H-01/H-02, U-07/U-09/U-10, and DL-01/CC-01.
