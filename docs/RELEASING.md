# Releasing VCollections

How VCollections is published to Maven Central (releases and snapshots). GitHub
Actions only builds, tests and checks the publication locally
(`.github/workflows/ci.yml`); publishing is done by hand with the scripts below,
the same way as for VMF.

## Artifact and version

`eu.mihosoft.vcollections:vcollections`, built by the single Gradle project of
this repository. The version is `publication.version` in
`config/common.properties`; between releases `main` carries the next version as
`-SNAPSHOT`.

VMF's runtime depends on VCollections. A VMF release can only depend on a
released VCollections version: release VCollections first, then update the
dependency in VMF (`runtime/build.gradle`).

The Sonatype namespace is `eu.mihosoft`. It belongs to an organization that also
publishes VMF, VMF-Text and other projects, and the Central Portal enforces
**monthly publishing limits per organization** (about 7 release events, ~1,100
files and ~78 MB per calendar month; the Usage Center at
<https://central.sonatype.com/publishing/usage> is authoritative). Releases are
therefore batched; snapshots are the channel in between.

## Credentials

The scripts read a Gradle properties file and pass its entries as `-P`
arguments. Nothing is copied into `~/.gradle`. The default is
`/f/gradle.properties` (`F:\gradle.properties`, an encrypted drive: mount it
first); set `VCOLLECTIONS_PUBLISH_PROPS` to use another file.

| Property | Used for |
|---|---|
| `mavenCentralUsername`, `mavenCentralPassword` | Central Portal user token (uploads, staging API, Portal API) |
| `signing.keyId`, `signing.password`, `signing.secretKeyRingFile` | GPG signatures (required for releases; snapshots are signed too if present) |

## Snapshots

Snapshots go to the Central snapshot repository,
<https://central.sonatype.com/repository/maven-snapshots/>. They are plain
uploads: no validation, no staging, no hand-off. Every push adds a timestamped
build that consumers pick up as the newest `-SNAPSHOT`, and builds are deleted
after 90 days. Snapshot uploads **do not count toward the monthly limits**
(verified with VMF-Text on 2026-10-20).

Publish a snapshot from a clean `main` whose `sh ./gradlew test` is green:

```sh
sh scripts/publish-central.sh
```

To check which build is live and which commit it was built from
(`Build-Revision` in the jar manifest), no credentials needed:

```sh
sh scripts/central-staging.sh snapshots
```

Publish snapshots from `main` only, so that the snapshot repository always shows
a merged state; try branches with `publishToMavenLocal`.

### Using a snapshot

```groovy
repositories {
    mavenCentral()
    maven {
        url = uri('https://central.sonatype.com/repository/maven-snapshots/')
        mavenContent { snapshotsOnly() }
    }
}

dependencies {
    implementation 'eu.mihosoft.vcollections:vcollections:0.3.6-SNAPSHOT'
}
```

Gradle caches snapshots for 24 hours: use `--refresh-dependencies` to get a
build that was published in the meantime.

## Releases

Ask before publishing a release to Central: the monthly limits are shared.

1. **Git** (on `main`, after `sh ./gradlew test` is green):
   - set `publication.version = X` in `config/common.properties`,
     `git commit config/common.properties -m "release X"`
   - `git tag vX`
   - set the next `Y-SNAPSHOT`, `git commit config/common.properties -m "development version Y-SNAPSHOT"`
   - `git push origin main && git push origin vX`
2. **Maven Central**, from the tag (`git checkout vX`):

   ```sh
   sh scripts/publish-central.sh            # hand-off for manual review: press "Publish" on the Portal
   sh scripts/publish-central.sh automatic  # hand-off with publishing_type=automatic
   ```

   The script uploads the signed artifacts (jar, sources, javadoc, POM, Gradle
   module) into a staging repository, looks up its key
   (`scripts/central-staging.sh repos open`) and hands it off with
   `releaseToCentralPortal -PrepositoryKey=<key>`: one Portal deployment, one
   release event. The hand-off without a key
   (`.../manual/upload/defaultRepository/eu.mihosoft`) failed with HTTP 400 for
   this account. `releaseToCentralPortal` skips `-SNAPSHOT` versions.

   Check the result: <https://central.sonatype.com/publishing/deployments>,
   `sh scripts/central-staging.sh status <deploymentId>`, or
   `sh scripts/central-staging.sh published X` (`"published":true` after a minute
   or two; repo1.maven.org follows within about 30 minutes).
3. `git checkout main`.
