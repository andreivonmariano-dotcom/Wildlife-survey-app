# LIKAS | Product and User Experience Critique

**Reviewed:** 28 October 2026 (Asia/Manila)
**Source:** RC4-derived `feature/likas-core-fieldwork-post-rc4` at draft PR #38
**Boundary:** Source inspection of Android activity structure, visible labels and interactions. No installed APK screenshots or phone handling were reviewed. **No startup animation, background art or launcher icon changes.**
**Decision status:** Product-design recommendations, **not accepted feature instructions or implemented changes**. Test planning deferred at the owner's direction.

## Product judgment

LIKAS contains broad field-science functionality, yet the visible Android experience still presents separate utilities instead of one coordinated fieldwork process. The application acts first as a wildlife survey recorder with catalog and research-export extensions. The original all-in-one biodiversity goal spans species identification, habitat and ecological interpretation, and up-to-date research information. Some of those capabilities exist only as supporting tools or future goals.

**Design direction:** Make the user's current work context visible, bring recording first, place research references one action away, and separate source evidence from calculated scientific inference.

## High-priority critique

| Severity | Source-screen observation | Cost to field biologist | Design recommendation |
| --- | --- | --- | --- |
| High | `WelcomeActivity.home` gives four group tiles, two shortcuts and **More tools**, with projects, stations, study rules, audit, dashboard and media tools hidden. | Site and project management can be harder to locate than a new survey. The screen feels like a list of features instead of a working console. | Put **Resume / Start survey**, **Current project / station**, and **Recent fieldwork** first. Give Catalog and Field Library clear destinations. Place administrative options in Tools. |
| High | `WelcomeActivity.setup` displays radio choices for several methods, project selection, checklist area/GPS lookup, route length, station spacing, bearing, study rules and extra fields in one long layout. | High setup effort and reading burden during field deployment. | Stage work into **Method → Site and effort → GPS / Start**. Show just the method's required fields and put optional settings behind one meaningful disclosure. |
| High | `MainActivity.draw` combines two clocks, GPS, pause, station navigation, haptics, tracking, mark time, bearing, calls, devices, evidence, taxon input, counts, identification confidence and photo/documentation. A fixed three-action footer keeps Save, Review and End accessible. | The always-present footer is good, but the two status/action regions reduce vertical recording space. Less common controls appear near high-frequency observation entry. | Preserve the fixed Save and End actions. Separate the **Quick observation** strip from method-specific measurements and attached evidence. Keep status compact and selectable for detail. |
| High | `SpeciesPickerActivity.addSpecies` foregrounds a **% screening index** and green “Likely” label. `HabitatLikelihood.assess` can assign **60/100** and “Likely” from a province source listing alone, before reviewed ecology matches. The code states the index is not calibrated occurrence probability. | A prominent percentage and “Likely” still resemble an occurrence prediction. Field users can overinterpret a national/province checklist as local site presence. | Replace percent/“Likely” presentation with **Source-listed in province / island**, **Habitat supports / conflicts / not assessed**, and **Elevation assessed / not assessed**. Retain the documented ranking logic only as a private sort aid until scientifically calibrated. |
| Medium | `CatalogActivity` begins with **Species checklists**, then requires **Find species information**, then opens another species-reference search. The survey picker and reference finder are separate browsing routes. | Extra navigation for common species lookups, inconsistent purpose of “Identify species” versus “reference”. | One searchable **Species Library**, with explicit **View reference** and **Use in current observation** actions. Label identification aids as references rather than automatic determinations. |
| Medium | `CatalogActivity.refresh` builds text using Java literal `"\\n"`, then replaces `"\\\\n"` with `"\\n"`. The text builder has single literal slash-n separators; the replacement targets double slash-n separators. Its divisor **“1503 catalog entries”** is hard-coded. Initial text always says **“Available offline · ready for field use”**, even if an individual source group is missing. | Possible visible `\n` characters, a stale ecology denominator, and a contradictory readiness message. These are **source-confirmed formatting/state defects**, not tested screenshots. | Render true line breaks or separate rows, compute installed source count from SQL, and show “Incomplete catalog” when a required group has zero sourced records. |
| Medium | `DashboardActivity` shows numerical cards, counts and recent rows. `HistoryActivity` has full-state filters, paginated survey/observation records and closure audit, but fieldwork navigation remains list-centric. | Limited direct answers to “What did I finish at this project and station?” and “What needs my attention?” | Add project-and-site filters, a concise work-status summary, and review-needed links. Never call these counts abundance or density estimates. |
| Medium | `AuditActivity` now uses a background executor on the core branch. `DashboardActivity.render` and multiple `HistoryActivity` routines still execute aggregate/read queries on the UI thread. `MainActivity` spans **2,413 Java lines**. | More difficult to maintain and a responsiveness risk with large archived datasets. | Extract a reusable read-only project/survey repository and a survey-session coordinator incrementally. Maintain existing outcomes and original records. |
| Medium | Home calls itself **“Philippine Wildlife Survey”**, and no dedicated Habitat/Ecology Library or Research Watch Activity is present in the inspected activity tree. | The current information structure underrepresents the owner's all-in-one biodiversity vision. | Articulate **Fieldwork**, **Species**, **Habitats**, **Research**, and **Data** as the long-range information structure. Do not advertise unfinished sections as functional. |

## What the current design gets right

- Survey **Complete / Incomplete / Not Counted** is explicit, preserves archived excluded records, and retains a source audit.
- Live recording keeps **Save Observation**, **Review Records**, and **End Survey** fixed. This directly addresses the earlier problem of an inaccessible end-survey action.
- A current active survey takes priority on Home through **Resume Survey**, rather than silently opening another event.
- The app permits explicitly flagged GPS-missing survey starts and records coordinate quality. No GPS reading is silently invented.
- Survey methods depend on wildlife group, and several detailed forms already use disclosure sections. Small-screen survey tiles and form controls use size-aware logic.
- Taxonomy, occurrence evidence, ecological screening and scientific records have separate underlying storage and source labels. Recent core-branch corrections prevent imported `unreviewed` locality rows from acting as positive source-listed presence.
- Species records include source editions and earlier names. Survey records keep verbatim identification fields instead of automatically rewriting them.

## Five screen-level design decisions proposed for review

**Home:** One prominent active survey or Start Survey; smaller Current Project, Recent Fieldwork, Species Library and Field Library entry points. Group scientific audit, import and settings behind an explicit Tools section.

**Survey Setup:** Progressive screens with method first, then site/station/effort, then GPS preflight. Display only inputs that the chosen method uses. Preserve template snapshots and advanced field parameters.

**Live Recorder:** Compact GNSS + session/effort status at top; first visible task is record species/detection/count. Preserve fixed Save and End. Evidence capture and method measurements open by context, not as an undifferentiated sequence of buttons.

**Species Library:** Search/compare by name, synonym, family and group. Present cited local occurrence and ecology separately. No unsupported probability graphics. A published source presence is never a confirmed site detection.

**Fieldwork History:** Organize by project and station, then survey. Surface outcome, last recorded effort, number of detections, missing GPS/evidence, and original audit trail. Hide deep record edits behind an explicit Review action.

## Scientific product boundaries

- A species **source listing** establishes checklist evidence at its documented geographic scope, not presence at a GPS point.
- **Identification** needs diagnostic traits, reference images/audio rights, expert determination history and source citations, not only a searchable Latin-name list.
- **Habitat match** is an evidence screen, not validated occurrence probability or modeled absence.
- Research news must be dated, source-linked and clearly cached/offline. No live research feed is present in the inspected feature-tree.
- Do not sacrifice archived observations, protocol completion semantics, GPS metadata or research exports to obtain a cleaner screen.

## Recommendation for next design conversation

Review **Home**, **Survey Setup**, then **Live Recorder** as three consecutive workflows, focusing on information order and interaction count before styling. The next design pass should produce concrete revised screen structures and labeled component behavior. Actual artwork and startup animation remain owned by the separate conversation. **No test-plan work, schema edits, or APK release is authorized by this review.**
