# LIKAS RC53 | Screen 2: Fieldwork layout audit
**30 October 2026 (Asia/Manila).** RC53 isolated draft from RC52 Home verified preview. This is an actual Android layout pass, not a mockup.

## Scope and traceability
- Repository `andreivonmariano-dotcom/Wildlife-survey-app`. RC53 branch `feature/likas-rc53-fieldwork-screen2-theme`, draft PR #50 into RC52 #49, itself draft into RC51 #48 and RC50 #47. Core integrated RC49 is unaffected.
- The owner requested a one-screen-at-a-time theme audit. Screen 1 Home completed in RC52 (real Android screenshot pass). RC53 touches only the Fieldwork hub and related source/Android QA, not active surveys' input forms, lifecycle, storage, source artwork, or scientific analysis.
- Existing actual RC49 Fieldwork screenshot at 320dp and 200% font showed Birds, Herpetofauna and Mammals large stacked cards, the Records heading partly below the fold, and records at different widths. RC52 source had a two-column layout on wider screens and different horizontal/vertical styles for the third taxon. This was a cross-device inconsistency, not a demonstrated data integrity failure.
- Requirement areas: visual consistency and readability (U), accessible action sizes (A), active-survey visibility and lifecycle (S), offline navigation/field usability (G), approved graphic integrity (L).

## Design change
- Replace the mixed 2-column/new-survey card system with **three identical full-width, horizontal, clickable rows** using the existing pixel art mapping for Birds, Herpetofauna, Mammals. Preserve actual method choices `()->setup(...)` and all accepted field protocols. Text and card heights remain dynamically measured, so 200% font may increase scroll length instead of clipping content.
- Organize **Records & projects** as uniform full-width actions: Projects & stations, History (latest survey outcome summary), Analysis (survey summaries). No second History action added. The single History action still uses its existing real database-derived summary and destination.
- Active survey state: **Current survey**, concise **In progress** status, preserved exact profile/method/project metadata, and the same primary **Resume Survey** Intent. New-survey choices stay hidden whenever an active event exists. Fieldwork continues to expose History and Analysis, with no survey-state mutation.
- **More field tools** retains the persisted collapsed/expanded preference and all five destinations: Site habitats, Audio, Camera traps, Protocols, Study plans. Removes the duplicated Field tools subheading shown only when expanding the button. The label and screen reader description reflect expand/collapse.
- Use `UiKit` and `LikasDesignSystem` already agreed in RC52. No redesign of startup or species info. No APK permission, package, signer, SQLite schema, biology, reference data, observation, or export behavior changed.
- Preview build advances to `0.27.3-rc53-fieldwork-hub-preview`, versionCode **113**, package remains `org.phwildlife.survey.beta08`, database schema **27**, owner-approved original PNGs and all assets unchanged.

## Acceptance criteria
1. Actual Android 14/API34 screenshots at 360dp/100%, 412dp/130%, 320dp/200% with scrolled tools expanded. Visually inspect text wrapping, full-width actions, card density, background, icon cropping and navigation continuity.
2. All three survey group actions and Records/Analysis/Projects remain visible/reachable through ordinary vertical scrolling, with 48dp+ tap targets and accessible labels.
3. Synthetic active survey in disposable emulator keeps Resume and does not offer new-survey choices; fixture removed after check. End Survey in live recorder remains untouched.
4. Existing Home/Birds setup and RC51 actual ZIP scientific regression pass. Build with original signing identity. No historical user data present in emulator.
5. Pending physical handset: one-handed navigation, sunlight/glove interaction, TalkBack, 200% font and installed upgrade on historic records. Draft until evidence is reviewed.

## Results
Pending current Android 14 CI run. Source tests changed intentionally to enforce single-column Fieldwork instead of previous two-column choices. Earlier CI run `41052039709` stopped on four obsolete source-layout assertions before Android compilation; these were updated without reverting the design. Do not claim that first run as a successful APK release.

## Next screen
If RC53 passes, Screen 3 will audit the survey-setup interface for Birds, Herpetofauna and Mammals, including method cards, field labels, numeric units, native keyboard focus, validation messages, vertical scrolling and the pinned Continue to GPS action. Keep scientific behavior unchanged and preserve reversible field data.
