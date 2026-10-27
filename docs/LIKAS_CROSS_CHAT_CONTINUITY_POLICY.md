# LIKAS: Cross-Chat Continuity and Project Memory Policy

**Policy ID:** CC-01  
**Adopted:** 27 October 2026  
**Applies to:** Every LIKAS development conversation, including a newly opened chat after an earlier conversation reaches its limit.  
**Status:** Mandatory development procedure. This procedure creates persistent written context. It does not change the ChatGPT account or Project memory setting.

## Purpose

A new LIKAS chat must continue from the latest recorded, verified project state without asking the project owner to repeat decisions already documented. Conversations are temporary working spaces. The GitHub repository stores the durable technical record.

## Required reading at the start of every development chat

Before drafting a change, proposing implementation, modifying code, or reporting app status:

1. Open the **connected LIKAS GitHub repository**, `andreivonmariano-dotcom/Wildlife-survey-app`, and verify its current default-branch HEAD. Do not infer the current state from an old conversation or a previously generated file.
2. Read repository `AGENTS.md`, `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md`, `docs/LIKAS_CROSS_CHAT_CONTINUITY_POLICY.md`, `docs/LIKAS_PROJECT_STATE.md`, and `docs/LIKAS_DECISION_LOG.md`. Read `docs/LIKAS_DIRECT_DOWNLOAD_POLICY.md` for deliverables.
3. Check the current `README.md`, relevant code, tests, and QA documentation before claiming that an app feature has shipped.
4. Use other available LIKAS chats and attached Project sources where they can be accessed. Treat earlier chat recollection as supplemental rather than a substitute for the committed state files.
5. Determine the newest verified baseline and the user's latest explicit direction. Resume from the recorded *Next actions* section without making the user restate known details.

A new conversation may start with a short recap of the most recent completed task and the current next step. Do not give a long onboarding response or ask the user to train the chat again.

## Required state updates during a chat

Update `docs/LIKAS_PROJECT_STATE.md` **in the same working session**, whenever one of these occurs:

- The project owner accepts or rejects a feature, interface, asset, methodology, or priority.
- Code, database schema, build configuration, app assets, test coverage, or documentation changes.
- An APK or downloadable asset is built, tested, or delivered.
- A bug is discovered, investigated, fixed, or left unresolved.
- A task becomes blocked or is left partially complete.

Append a concise, dated entry to `docs/LIKAS_DECISION_LOG.md` for durable decisions or reversals. Do not rewrite old entries to conceal a prior decision. Link to a change commit or source where possible.

**Do not wait for a closing message or a warning that a chat is near its limit.** Save progress after meaningful milestones, since an ended conversation cannot necessarily issue a final automatic handoff.

## State file contract

The current-state file must be concise, accurate, and readable by a new chat. Keep:

- Last verified date (with time zone), repository, branch, observed HEAD SHA, and last app-code commit when known.
- Current release/version and database schema with source and verification state.
- Product vision and high-priority scientific and visual requirements.
- Implemented and verified work, implemented but untested work, planned features, and blocked tasks, clearly separated.
- Latest accepted decisions, conflicting/older decisions that were replaced, and governing references.
- User-approved assets/checklists, their source locations, and whether they are present in the repository.
- Current bugs, possible regressions, release readiness, and outstanding field tests.
- Immediate next action and the last unfinished task, written with enough context to continue.
- The status of artifact handoff. **A former chat's sandbox download URL must never be reused without verifying that the file is actually accessible in the current session.**

When the repository changes outside this chat, verify the latest code first. Never overwrite the state file with assumptions copied from old notes. Record discrepancies explicitly.

## Source-of-truth priority

For the current instruction, use this order:

1. The project's latest explicit decision from the user.
2. The Master Project Specification and mandatory scientific or delivery rules.
3. The newest verified repository code, tests, and build artifacts for claims about what exists.
4. The current project-state file and append-only decision log for goals, decisions, and unfinished work.
5. Earlier chat summaries for context not contradicted above.

Distinguish **requested**, **coded**, **tested**, **field-verified**, and **released**. A commit alone is not proof that an APK was built, deployed, or tested on a phone.

## Transition and recovery

When the user starts a new LIKAS chat and says "continue," "resume," or names an earlier chat, first read the current-state and decision files. Continue the requested work using the latest verified state. Do not ask the user to paste prior conversation histories if the needed details already appear in those records.

If the state files are stale or the GitHub connection is unavailable, describe the precise gap. Work with the context that is available. Provide a self-contained, copyable handoff summary for the next chat and save updates as soon as access returns. Never invent unseen prior decisions or claim complete knowledge of a previous conversation.

At the end of substantial work, report three compact items: what changed, what remains unverified, and the next concrete task. Synchronize the durable state files first.

## ChatGPT Project memory setup

In **Project settings > Memory**, select **Project-only memory** if this option is available. In **Settings > Personalization**, turn on the memory or chat-history settings required by the account. Project memory can help surface previous LIKAS conversations but is not a guarantee of exact transcript recall. This setting is controlled by the project owner, not by GitHub files or assistant instructions.

Put the companion `LIKAS_PROJECT_INSTRUCTIONS.txt` rule into the project's **Project instructions** and keep these continuity files available as Project sources or through the connected GitHub repository. Files saved only to a personal Library are not automatically Project attachments.

## Relationship to other policies

- Scientific and product governance: `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md`
- Direct downloadable files: `docs/LIKAS_DIRECT_DOWNLOAD_POLICY.md` (DL-01)
- New-chat continuity and state recording: **CC-01** (this file)
- Implementation-specific handoff: `docs/LIKAS_PROJECT_STATE.md`
- Durable choices and reversals: `docs/LIKAS_DECISION_LOG.md`

**Acceptance check:** Open a fresh LIKAS chat. Ask it to read the repository state and identify the latest app baseline, last accepted visual direction, unresolved work, and next task. It must provide a consistent answer without claiming that unfinished work is shipped.