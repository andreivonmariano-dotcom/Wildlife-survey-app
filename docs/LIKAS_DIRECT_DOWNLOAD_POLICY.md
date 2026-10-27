# LIKAS: Mandatory Direct Download Delivery Policy

**Policy ID:** DL-01  
**Effective:** 27 October 2026  
**Status:** Mandatory project instruction  
**Applies to:** Every LIKAS project conversation, developer, assistant, build, and release handoff.

## Non-negotiable user requirement

**Whenever an APK, image, ZIP, animation, document, export, or any other downloadable file is presented to the user, supply a working, direct file-download link in the chat. Never make the user visit GitHub or another external website to obtain the file.**

A GitHub repository URL, release page, Actions run, artifact listing, commit page, Google Drive preview, or similar navigation page is **not** a substitute for the direct file. A button or link that opens a webpage instead of delivering the requested file does not satisfy this policy.

## Required delivery workflow

1. **Produce the actual artifact.** For APK requests, use the newly built APK matching the named LIKAS version and code revision. Do not present an older binary as a new version, a filename as proof of a build, or a source-code link as an APK.
2. **Place the artifact into the active conversation's downloadable file space.** Copy or export each deliverable to `/mnt/data/` in the current runtime. Use a descriptive name, for example `LIKAS-v0.16.0.apk`, `LIKAS-frog-icon.png`, or `LIKAS-approved-assets.zip`.
3. **Verify the exact file.** Confirm that the file exists at that exact path, is nonempty, and matches the advertised type. For APKs, check the app package/version, build completion, and signing continuity where tools permit. Do not invent paths, links, sizes, checksums, or build results.
4. **Present an immediate, clickable, direct link.** Use Markdown sandbox links to the verified file, for example `[Download LIKAS v0.16.0 APK](sandbox:/mnt/data/LIKAS-v0.16.0.apk)`. The link must point to the file itself, not to a repository, release, or website. Treat the example filename as illustrative, never as permission to invent a file.
5. **Link every deliverable separately.** APK, PNG, GIF, WebP, ZIP, PDF, CSV, and other delivered files each receive their own direct link. For multiple approved assets, provide direct individual files or a ZIP containing exactly the approved files, as requested.
6. **Do not substitute screenshots or previews.** Inline image previews are welcome, but an image claimed as a downloadable deliverable needs a direct file link too.
7. **Treat external links as supplementary.** A GitHub commit, repository, or CI run may be cited for source tracking only after the primary direct-download link. Never label an external site as the download button or leave the user to locate the file there.
8. **Report a delivery blocker plainly.** If the artifact cannot be built, accessed, copied, verified, or attached to this chat, do not claim it is available. State exactly what succeeded and what prevented direct delivery. Do not offer a GitHub page as a substitute or fabricate a sandbox URL.
9. **Make each chat self-contained.** A file linked in an earlier chat is not a current downloadable artifact until its exact local path is verified again or a fresh copy is attached. Do not assume a CI artifact is accessible through a GitHub viewer link.
10. **Respect safe software updates.** A new APK download must not silently change the LIKAS package identity or signing key. Label build variants and say whether on-device installation or upgrade testing remains unverified.

## Release response format

When a new version is actually available for direct download, the response must provide:

- The LIKAS version and build variant.
- **A direct downloadable file link** to the verified APK or requested asset, placed prominently near the start.
- A concise summary of the changes and any known testing limitations.
- Optional source commit or changelog links only as additional references.

## Acceptance test

**Pass:** Selecting the download link starts delivery of the actual file within ChatGPT without having to browse an external page, find an artifact, or sign into GitHub.  
**Fail:** The response provides only a GitHub URL, repository/releases page, CI job, web preview, filename without an attached file, broken sandbox path, or an old APK misrepresented as new.

**Authority:** This delivery rule supplements `docs/LIKAS_MASTER_PROJECT_SPEC_v1.0.md` and is mandatory for downloadable-artifact handoffs. It does not waive scientific, privacy, data-integrity, or build-validation requirements.