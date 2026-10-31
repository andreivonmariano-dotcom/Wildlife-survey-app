# LIKAS RC28: Minimal Research UI and APA Author–Date Display

**Owner direction:** 29 October 2026. Current Android candidate `0.27.3-rc28-minimal-apa-ui-preview`, versionCode 88. Core branch PR #38. No startup/launcher assets are modified.

## Product intent
Treat LIKAS as a field instrument and academic reference library, not a publisher directory. Show the biological fact first. Put an APA 7 author–date citation beside each factual claim. Place a deduplicated, alphabetically ordered reference list behind one obvious **References & study details** disclosure. Preserve bibliographic provenance and research limits without repeating a Source line under every paragraph.

### Example before and after
**Before:** “Bat Conservation International describes distinctive gold-colored fur on the head of the giant golden-crowned flying fox.” Then “Source: Bat Conservation International, Acerodon jubatus species account.”

**Updated display:** “Distinctive golden fur covers the head of the giant golden-crowned flying fox (Bat Conservation International, n.d.).” The full source record is accessible under **References**, linked to the original URL. No publication year is inferred from an access date.

**Published-paper example:** “A Philippine Eagle pair studied at Mount Sinaka incubated one egg for 58 days; the female accounted for 74% of observed incubation time (Ibañez et al., 2003).” Scope remains one observed pair, not a population-wide invariant.

## Citation rules
- **APA 7 author–date style** in the reading view: `(Author, year)`, `(Author & Author, year)`, `(Author et al., year)`, and `(Group author, n.d.)` for no documented date.
- Use recorded publication year, not device clock, retrieval year or an inferred year.
- Use a single alphabetical references panel per species account, deduplicated by source URL.
- Keep optional unrelated literature as **Further reading**, not duplicate citations already supporting visible claims.
- Show verified full APA references where complete authors, article title, publisher/journal, year and DOI are available. Incomplete third-party records remain traceable source entries, not fabricated full APA references. Metadata enrichment is a follow-on task.
- Maintain source labels, URL/DOI, source type, source year, source-location excerpts, review status and study limits in underlying assets. Export and scientific review must not lose provenance.
- Group-level habitat citations follow each relevant description, with the source bibliography at the end. Country/island references never establish an individual observation.

## Minimalist design rules
- Off-white field background, deep green primary text, subdued borders, white data cards, one readable type hierarchy.
- Back navigation uses a compact 48dp arrow target with a full spoken accessibility description. Long headers align left.
- Short, natural button labels. Home prioritizes Fieldwork, Workspace, Species Library and Habitats. Backup, deletion, source catalogs and specialist tools remain discoverable under **More tools**.
- Information appears in short, meaningful sections. Group study caveats and bibliographic provenance behind progressive disclosures. Do not hide critical survey controls.
- Secondary expansions use 160ms ease-out fades. Page entry can use 180ms restrained fade. Disable movement at 160% or greater text scale and when system animations are disabled.
- End Survey, Save Observation and survey Complete/Incomplete/Not Counted states remain unchanged. No changes to SQLite 27 or user survey records.

## Engineering implementation
- `ResearchCitationFormatter.java`: safe author-year formatting, no invented years, source-list fallback for incomplete citations.
- `Rc11PrioritySpeciesView.java`: claim + inline citation, one references panel, further reading deduplicated against cited URLs, short readable body text.
- `Rc14Bibliography.java`: partial APA records and independently sourced further reading.
- `OfflineFieldLibrary.java`: habitat author–date citations and grouped references.
- `Rc8TraitComparisonActivity.java`: inline claim citations and consolidated clickable source URLs.
- `Rc13FieldGuideCopy.java`: 29 exact, reviewed display-only editorial rewrites of publisher-led sentences. Raw 456 original fact records remain untouched.
- `UiKit.java`, `UiMotion.java`, `WelcomeActivity.java`, `TaxonDetailActivity.java`, `HabitatRecordsActivity.java`: consistent visual system, accessible motion and simplified Home.
- New JUnit source formatter tests and Python UI/source-preservation contracts.

## Release gate (not a field certification)
Check the exact RC28 Android source HEAD. Require Python source/scientific tests, JVM unit tests, signed Android APK assembly, certificate and manifest versionCode confirmation, downloadable APK in current chat, and the existing 16-test Android14 320dp emulator suite, including 200% font and 96 species renders. Preserve pinned certificate `f7c471ad310aeeffd2b2a7cf7c371c6efdce258560a818b52be5e1f2a7509937` and package `org.phwildlife.survey.beta08`. No real-owner handset upgrade, full backup restoration, GPS/audio or field acceptance is implied by a passing source build.

## Known outstanding work
Complete APA 7 reference lists need bibliographic enrichment for many original source labels, including those with only an `et al.` summary rather than full author names or a verified article title. Audit remaining legacy non-priority species pages and non-species research screens for repeated Source blocks. Inspect the new first viewport and landscape/200% font for clipping. Maintain separate startup workstream.
