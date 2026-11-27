# APO | Design and layout playbook v1.0
Date: 27 November 2026 (Asia/Manila)
Status: Development guidance implementing charter v1.3, especially APO-05, 16–19, 27–30. This companion does not amend the charter or approve a new feature, brand, framework, or release. All examples are proposed design defaults, not screenshots or tested features.

## 1. The product experience
APO should feel like a Philippine natural-history field guide with a dependable field notebook. A student or interested naturalist should be able to explore it immediately; a biologist should be able to collect scientifically useful data without navigating a technical dashboard. Put the user's task before the software's internal structures. A complex database, scientific method, or map layer is never a reason to expose implementation details on a primary screen. Keep necessary scientific terms, uncertainty and required inputs, but use ordinary labels and short explanations where needed.

## 2. Information architecture and layouts
Use the approved product destinations: Fieldwork, Atlas, Map, Research Library. Keep My Data reachable through a consistent route. Use persistent labeled bottom navigation on compact phones for top-level destinations and a rail on larger windows when appropriate. Preserve navigation context and Back behavior. During recording, reserve the screen for the active survey and keep End Survey and resume accessible; navigation must never discard a draft. Avoid a second competing navigation system, unexplained icon buttons and duplicate paths serving the same purpose. Contextual cross-links are useful when they lead directly to related content.

Use a single main reading or form column on compact phones, a list-detail arrangement where width permits, and adaptable media grids for galleries. Align headings, text, images and controls to shared margins; start with 16 dp compact margins. Use consistent spacing with a proposed 4/8/12/16/24/32 dp scale. Group related material by proximity before adding card borders. Give each screen one visually dominant purpose, with secondary actions near their content. Use filled buttons for the primary action, subdued buttons for secondary actions, and clear confirmation for consequential actions. Do not rely on swipes or long presses for essential functions. Keep controls clear of system bars, keyboard and display cutouts. At 200% font size, reflow or stack content and allow scrolling rather than shrinking text or clipping controls.

## 3. Visual system
Retain the approved APO natural-history identity: restrained forest greens, warm neutral surfaces and authentic wildlife photography. A consistent design is more valuable than decorative variation on every screen. Define shared semantic colors for primary action, surface, text, outline, selected state, error and warning, with accessible dark and light values. Use color plus words or shape for meaningful states. Prefer restrained surfaces, few competing accents, limited shadows, and clear alignment. Avoid gradients, glass effects, heavy card nesting, ornamental icon sets and animations that obscure a task. Make motion optional and purposeful.

Proposed starting type scale: page title 24–28 sp, section title 18–20 sp, body 16 sp, secondary label 14 sp. Keep scientific names correctly styled and fully readable. Use a legible system sans serif for controls and body; any editorial display face must be approved and tested. On wide windows constrain long reading lines, with around 60–75 characters as a starting range. These dimensions are starting points, not fixed limits. Respect charter accessibility: 48 dp touch targets, required contrast, 200% scaling, accessible names and focus order. Use one family of familiar icons with visible labels for major navigation and unfamiliar actions.

## 4. Screen patterns
Home: short welcome or heading, Start Survey or Resume Survey as the primary action according to session state, quick access to other destinations, and optional featured wildlife. No forced startup animation, technical status panel, promotional carousel or repetitive introduction.
Fieldwork setup: select project and method, then show only relevant sampling requirements. Reuse known project/site details with explicit user control. Ask each required question once, group short related inputs, and preserve answers on Back. Use persistent field labels, input units, appropriate keyboards and inline validation. Avoid redundant help text and mandatory tutorials.
Active survey: show elapsed/required effort and current station or route only where applicable; give the observation form priority. Save and End Survey remain accessible. GPS summary can state availability and meaningful accuracy; full fix metadata is available in details. Preserve manual/unresolved identifications, method-required fields, actual counts, and explicit Complete, Incomplete and Not Counted outcomes. Show save feedback only after durable persistence. Keep draft recovery and correction paths clear.
Atlas: a single search entry with useful filters, a scannable species list, then a detail page. Place approved photo, common/scientific name, key identification clues, habitat and distribution first. Keep dated conservation labels separate. Use source-qualified links for ecology and geography; citations and deeper methods appear in a plainly labeled details area. Keep scientific uncertainty visible when it changes meaning. Do not invent imagery or records.
Map: allow the map itself to occupy most of the screen. Show a concise legend, a clear layer selector and a selected-place detail sheet or pane. Distinguish curated geographic reference from user localities, observed evidence and modeled data. Keep layer date, source, resolution and sensitive-site controls available. No permanent GIS toolbar or developer labels on the default screen.
Research Library: searchable title/author/topic listing with year and an explicit local reading availability label. A paper detail provides bibliographic metadata and rights-cleared text or permitted summary. Keep links that need a connection clearly marked. My Data: prioritize recent projects, survey history, export and backup; use meaningful status labels rather than schema names.

## 5. Copy and disclosure
Use short, specific, actionable labels. Write "Location unavailable" with an available next action rather than displaying an exception code. Write "Observation saved" only after the write succeeds. Show required method information at the point of action. Move advanced diagnostics, source history, coordinate metadata and uncommon settings into a clearly named detail view, usually one interaction away from the relevant object. Avoid deep nested disclosures and invisible controls. Preserve original archived sources and all scientific distinctions under APO-09–12. Never simplify by silently dropping measurements, required fields, evidence, uncertainty or access to survey completion.

## 6. Development and evidence
Before implementing: record screen/task, current source revision, actual current screenshots when available, duplicate controls and task blockers, proposed layout, data invariants, and acceptance tests. Use native components and shared design tokens; document each exception. Compare before/after captures on the same device, data and settings. Test normal and 200% text, 320 dp width, light/dark, keyboard open, rotation, airplane mode, interrupted recording and relevant accessibility services. Review the running UI for alignment, typography, overflow, touch size, contrast, control hierarchy, redundant text, meaningful warnings and discoverability. Automated scans and screenshots alone do not establish successful interaction.

Observe representative field users and learners on realistic tasks without coaching: start/resume/end a survey, record and correct an observation, find a species including uncertain names, inspect habitat/source details, locate a saved site, find a local paper, export or back up records. Record completion, errors, assistance, time and a short ease rating; compare with the previous version using the same tasks. Document the sample and unresolved issues. No claim of better usability without actual comparative evidence. No claim of implementation or testing without the relevant source or test record. A release report identifies changed screens, exact commit and artifact, tests actually run, limitations and direct verified APK link when a build exists. Documentation changes do not imply an app update.

## 7. Screen review questions
- Is the current task immediately recognizable and is its main action discoverable?
- Can a first-time user understand the labels without software or GIS expertise?
- Does every visible element serve the task, and are related elements consistently grouped?
- Are necessary scientific distinctions, survey completion controls and error recovery retained?
- Are fonts, contrast, touch targets, focus and layout usable at supported sizes?
- Are claimed improvements demonstrated by the running app and recorded tests?

## Research register
Reviewed accessible source pages on 27 November 2026. Recommendations above adapt guidance to APO; proposed spacing and type scale are project design defaults, not measured performance or universal requirements.
- Android Developers, Layouts and navigation patterns: https://developer.android.com/design/ui/mobile/guides/layout-and-content/layout-and-nav-patterns
- Android Developers, Content composition and structure: https://developer.android.com/design/ui/mobile/guides/layout-and-content/content-structure?hl=en
- Android Developers, Design an Adaptive Layout with Material Design: https://developer.android.com/codelabs/adaptive-material-guidance
- Android Developers, Android color for mobile design: https://developer.android.com/design/ui/mobile/guides/styles/color
- Android Developers, Create an accessible and personalized theme and brand with Material Design 3: https://developer.android.com/codelabs/m3-design-theming
- Android Developers, Accessibility: https://developer.android.com/design/ui/mobile/guides/foundations/accessibility
- Nielsen Norman Group, Aesthetic and Minimalist Design: https://www.nngroup.com/articles/aesthetic-minimalist-design/
- Nielsen Norman Group, Progressive Disclosure: https://www.nngroup.com/articles/progressive-disclosure/
- Nielsen Norman Group, 5 Principles of Visual Design in UX: https://www.nngroup.com/articles/principles-visual-design/
- Nielsen Norman Group, Website Forms Usability: Top 10 Recommendations: https://www.nngroup.com/articles/web-form-design/
- Nielsen Norman Group, Error-Message Guidelines: https://www.nngroup.com/articles/error-message-guidelines/
- Nielsen Norman Group, How to Make Navigation Discoverable on Mobile: https://www.nngroup.com/articles/find-navigation-mobile-even-hamburger/
- Nielsen Norman Group, Icon Usability: https://www.nngroup.com/articles/icon-usability/
- Nielsen Norman Group, The Anatomy of a List Entry: https://www.nngroup.com/articles/list-entries/
- GOV.UK Design System, Question pages: https://design-system.service.gov.uk/patterns/question-pages/

Source authority: canonical APO charter v1.3 and its GitHub mirror. Fieldwork, offline scope, PAIC sequencing, source rights, sensitive data and direct-download policies remain unchanged.