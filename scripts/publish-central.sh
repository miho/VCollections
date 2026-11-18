#!/bin/sh
# Publishes the version in config/common.properties to Maven Central
# (eu.mihosoft.vcollections:vcollections).
#
# - a -SNAPSHOT version goes to the Central snapshot repository: plain upload,
#   no staging, no hand-off, and it does not count toward the Portal's monthly
#   publishing limits
# - a release version is uploaded to the staging API and handed off as one Portal
#   deployment (counts as one release event)
#
# Run from a clean checkout of the commit to publish, after `sh ./gradlew test`
# is green on it.
#
#   sh scripts/publish-central.sh            # release: manual review, press Publish on the Portal
#   sh scripts/publish-central.sh automatic  # release: hand-off with publishing_type=automatic
#
# Credentials: VCOLLECTIONS_PUBLISH_PROPS (default /f/gradle.properties), see
# docs/RELEASING.md.
set -e
cd "$(dirname "$0")/.."
VERSION=$(grep -E '^publication.version' config/common.properties | sed -E 's/^[^=]*=\s*//' | tr -d '\r ')
PROPS=$(sh scripts/central-staging.sh props)
REV=$(git rev-parse --short HEAD)
echo "==> publishing VCollections $VERSION (commit $REV)"
if [ -n "$(git status --porcelain --untracked-files=no)" ]; then
  echo "WARNING: tracked files have uncommitted changes, the artifacts will not match commit $REV"
fi

echo "==> vcollections: upload"
sh ./gradlew publishMavenJavaPublicationToMavenCentralRepository --no-daemon $PROPS

case "$VERSION" in
  *-SNAPSHOT)
    echo "==> SNAPSHOT: no staging hand-off needed; on https://central.sonatype.com/repository/maven-snapshots/ within a minute:"
    sh scripts/central-staging.sh snapshots "$VERSION"
    exit 0 ;;
esac

echo "==> staging repositories (open)"
sh scripts/central-staging.sh repos open
KEY=$(sh scripts/central-staging.sh repos open | awk 'NR==1 && $1 != "(no" {print $1}')
[ -n "$KEY" ] || { echo "no open staging repository found" >&2; exit 1; }
TYPE="${1:-user_managed}"
echo "==> hand-off of $KEY (publishing_type=$TYPE)"
sh ./gradlew releaseToCentralPortal -PsonatypeNamespace=eu.mihosoft -PrepositoryKey="$KEY" -PpublishingType="$TYPE" --no-daemon $(sh scripts/central-staging.sh props | grep mavenCentral)
echo "==> released repositories"
sh scripts/central-staging.sh repos released
DEP=$(sh scripts/central-staging.sh repos released | awk '$3 != "None" {print $3}' | head -1)
[ -n "$DEP" ] && { echo "==> deployment $DEP"; sh scripts/central-staging.sh status "$DEP"; }
echo "==> done. Check https://central.sonatype.com/publishing/deployments and the Usage Center."
