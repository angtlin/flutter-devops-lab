# Versioning & Release Process

This document explains how versioning works in this Flutter project and how to correctly cut a release.

---

## How Flutter versioning works

Flutter versions are defined in `pubspec.yaml` as a single string:

```yaml
version: 1.2.3+42
```

| Part | Flutter name | Android mapping | Example |
|---|---|---|---|
| `1.2.3` | `versionName` | Shown in Play Store listing | `1.2.3` |
| `42` | `versionCode` | Used by Play Store to order releases | `42` |

In our CI pipeline the `versionCode` (`+N`) is **always overridden** by `github.run_number` at build time, so you never need to update the `+N` suffix manually. The `versionName` (`1.2.3`) is what you must keep in sync with the Git tag.

---

## Why `pubspec.yaml` must be updated for every release

The `versionName` in `pubspec.yaml` is read by Flutter at runtime. If your app displays its version (e.g., an About screen), it reads this value via `PackageInfo.version`. If `pubspec.yaml` says `1.0.0` but the Git tag is `v1.2.3`, the app would show the wrong version to users.

Our CI enforces consistency: **the version in `pubspec.yaml` must match the Git tag before the build proceeds.**

---

## Tag format

All release tags must follow semantic versioning with a `v` prefix:

```
v<MAJOR>.<MINOR>.<PATCH>
```

Examples of valid tags: `v1.0.0`, `v1.2.3`, `v2.0.0`

Examples of invalid tags: `1.0.0` (missing `v`), `v1.0` (missing patch), `v1.0.0-beta` (pre-release suffixes not supported by the CI trigger pattern)

The tag maps to `pubspec.yaml` by stripping the `v` prefix:

| Git tag | `pubspec.yaml` version |
|---|---|
| `v1.2.3` | `version: 1.2.3+X` |
| `v2.0.0` | `version: 2.0.0+X` |

---

## How CI validates the version

On every tag push, the `build` job runs a version consistency check **before** signing or uploading anything:

```bash
PUBSPEC_VER=$(grep '^version:' pubspec.yaml | awk '{print $2}' | cut -d'+' -f1)
TAG_VER="${{ github.ref_name }}"
TAG_VER="${TAG_VER#v}"

if [ "$PUBSPEC_VER" != "$TAG_VER" ]; then
  echo "❌ Version mismatch: pubspec.yaml=$PUBSPEC_VER  tag=$TAG_VER"
  exit 1
fi
```

This step only runs on tag pushes. It does not run on regular branch commits or pull requests.

---

## What happens when versions do not match

If `pubspec.yaml` and the Git tag differ, the `build` job fails at the version check step with:

```
❌ Version mismatch: pubspec.yaml=1.0.0  tag=1.2.3
Error: Process completed with exit code 1.
```

The downstream jobs (`publish`) are blocked and **nothing is signed, released, or uploaded to the Play Store.** You must fix the mismatch and push a corrected tag.

---

## Step-by-step release guide

Follow these steps in order. Do not push the tag before updating `pubspec.yaml`.

**1. Update the version in `pubspec.yaml`**

Open `pubspec.yaml` and update the `versionName` part. Leave the `+N` suffix as-is (CI overrides it):

```yaml
# Before
version: 1.0.0+1

# After (releasing 1.2.3)
version: 1.2.3+1
```

**2. Commit the version bump**

```bash
git add pubspec.yaml
git commit -m "Bump version to 1.2.3"
git push origin master
```

Wait for the CI `test` and `build` jobs to pass on `master` before tagging.

**3. Create and push the tag**

```bash
git tag v1.2.3
git push origin v1.2.3
```

**4. Approve the release in GitHub**

The `publish` job is gated behind a manual approval. Go to the Actions tab in GitHub, find the running workflow for your tag, and approve the `release` environment deployment. The AAB will then be uploaded to the Play Store internal testing track and a GitHub Release will be created.

---

## Valid release example

`pubspec.yaml`:
```yaml
version: 1.2.3+1
```

Git tag:
```
v1.2.3
```

What CI builds:
- `versionName` = `1.2.3` (from `--build-name` derived from the tag)
- `versionCode` = `<github.run_number>` (e.g., `47`) — automatically increasing, never conflicts with Play Store

---

## Common mistakes

| Mistake | Result |
|---|---|
| Pushing the tag before updating `pubspec.yaml` | Build fails at version check — re-tag after fixing |
| Forgetting to push the tag (`git push` without `origin v1.2.3`) | CI never triggers — no release created |
| Using an invalid tag format (e.g., `v1.0.0-rc1`) | Workflow trigger does not match — no CI run |
| Approving the GitHub release environment without QA sign-off | AAB ships to internal track immediately |
