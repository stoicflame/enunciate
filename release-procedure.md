# Release Procedure

There's no CI/CD for this repo (yet) — releases are done by hand, following these notes. This file is written for
future-me, but it's also the canonical reference for Claude Code when asked to help cut a release, so it's worth
keeping both the commands *and* the "why" up to date as the process changes.

1. Make sure any new modules are included.
   1. The root `pom.xml` `<modules>` list.
   2. `top/pom.xml`'s dependencies (this is the module that pulls in the "default" set of Enunciate modules for
      non-Maven consumers).
2. Bump the version everywhere: `old_version` -> `new_version` (e.g. `2.20.0-SNAPSHOT` -> `2.20.0`).
   - Versioning: a "normal" release bumps the **minor** version (`2.20.0-SNAPSHOT` -> `2.20.0`, next snapshot
     `2.21.0-SNAPSHOT`). Only bump the **patch** version (`2.20.0` -> `2.20.1`) when this is explicitly meant to
     be a patch release (e.g. a targeted fix on top of an already-released minor, with no other pending changes
     that warrant a full minor bump) — don't default to a patch bump just because it looks like the smaller step.
   - The version is declared exactly once "for real", in the root `pom.xml`'s `<version>`. Every module's own
     `pom.xml` only references it via `<parent><version>...</version></parent>` — Maven does *not* propagate a
     parent version bump to those references automatically, so every module pom needs the literal string replaced
     too. Do a repo-wide replace across all `pom.xml` files (skip `target/`), e.g.:
     ```
     find . -name pom.xml -not -path '*/target/*' | xargs sed -i 's/old_version/new_version/g'
     ```
   - `maven-release-plugin` isn't viable for this until ENUNCIATE-501 is fixed (which depends on upstream
     MRELEASE-669), so this manual sed-based bump is the workaround.
3. Run the *full* test suite: `mvn clean install -Penunciate-full-tests`.
   - Plain `mvn clean install` silently skips the tests that compile/run Enunciate's generated client-side code
     (C, C#, Objective-C, Ruby, PHP, JS). The `pom.xml` enforcer rule blocks `deploy`/`release` unless
     `activate.full.tests=true`, which the `enunciate-full-tests` profile sets — so this isn't optional before a
     release.
   - This requires the native toolchains for those generated clients to be installed locally (see the README for
     the Ubuntu package list: `libxml2-dev`, `mono-devel`, `gnustep`/`gnustep-devel`, `ruby`, `nodejs`, `php`).
4. Commit the version bump and tag it, e.g. `git tag -a v2.0.0 -m "Version 2.0.0"`.
5. Make sure your PGP key is published to https://keys.openpgp.org/ and that `gpg` can sign with it locally — the
   `release` Maven profile runs `maven-gpg-plugin` to sign every artifact during `mvn deploy`.
   - Deploying also requires OSSRH/Sonatype Central credentials configured for the `ossrh` server id in your local
     `~/.m2/settings.xml` (used by `distributionManagement` and the `nexus-staging-maven-plugin`).
6. Deploy: `mvn clean deploy -Penunciate-full-tests,release`.
   - Both profiles are needed together: `release` wires up javadoc/source jar attachment, GPG signing, and Nexus
     staging (with `autoReleaseAfterClose`, so a clean deploy publishes straight through, no separate
     close/release step); `enunciate-full-tests` is what satisfies the enforcer gate described in step 3 (the
     `release` profile has its own copy of that same enforcer check, gating the `deploy` phase specifically).
7. Push the commit and tag.
8. Reset the version back to the next `-SNAPSHOT` (following the same minor-vs-patch rule as step 2, e.g.
   `2.20.0` -> `2.21.0-SNAPSHOT` normally, or `2.20.1` -> `2.20.2-SNAPSHOT` if this was an explicit patch release)
   using the same repo-wide `pom.xml` sed as step 2, then commit.
9. [Create the release on GitHub](https://github.com/stoicflame/enunciate/releases), off the tag from step 4.
10. Make any announcements.
11. Update the wiki (separate repo/clone of `stoicflame/enunciate.wiki.git` — see `LOCAL_CONTEXT.md` for the
    local clone path if you keep one): replace `old_version` with `new_version` across it the same way, e.g.
    `find . -name "*.md" | xargs sed -i 's/2.0.0/2.0.1/g'`, then commit and push there too.
12. Upload any new schemas to the pages — **skip this step for a patch release** (a patch shouldn't change the
    config schema, so there's no new schema to publish).
    - The versioned XSD itself lives *in this repo*, at `top/src/main/resources/META-INF/enunciate-<version>.xsd`
      (one file per released version, going back to 1.x — check that the new version's copy was added as part of
      the release commit).
    - "Upload to the pages" means copying that file into the `schemas/` folder of the `gh-pages` branch (a
      separate branch/checkout — see `LOCAL_CONTEXT.md` for the local clone path if you keep one) and pushing
      it, so `http://enunciate.webcohesion.com/schemas/enunciate-<version>.xsd`-style config-file references
      resolve, e.g.:
      ```
      cp top/src/main/resources/META-INF/enunciate-<version>.xsd <path-to-gh-pages-clone>/schemas/
      cd <path-to-gh-pages-clone>
      git add schemas/enunciate-<version>.xsd
      git commit -m "add <version> schema file"
      git push
      ```
13. Update the [Getting Started Sample](https://github.com/stoicflame/enunciate-sample) — a separate repo, not
    part of this checkout (see `LOCAL_CONTEXT.md` for the local clone path if you keep one).
14. Update and publish the [Gradle Plugin](https://github.com/stoicflame/enunciate-gradle) — also a separate repo
    (see `LOCAL_CONTEXT.md` for the local clone path if you keep one).
    1. Update `build.gradle` to the new version.
    2. `gradle publishPlugins`.
    3. Update the docs in that repo the same way as step 11 (`find . -name "*.md" | xargs sed -i 's/.../.../g'`).
