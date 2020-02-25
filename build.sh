#!/bin/bash

set -e

# Builds of tagged revisions are published to sonatype staging.

# Travis runs a build on new revisions and on new tags, so a tagged revision is built twice.
# Builds for a tag have TRAVIS_TAG defined, which we use for identifying tagged builds.

# sbt-dynver sets the version number from the tag
# sbt-travisci sets the Scala version from the travis job matrix

# To back-publish an existing release for a new Scala / Scala.js / Scala Native version:
# - check out the tag for the version that needs to be published
# - change `.travis.yml` to adjust the version numbers and trim down the build matrix as necessary
# - commit the changes and tag this new revision with an arbitrary suffix after a hash, e.g.,
#   `v1.2.3#dotty-0.27` (the suffix is ignored, the version will be `1.2.3`)

# For normal tags that are cross-built, we release on JDK 8 for Scala 2.x
isReleaseJob() {
  if [[ "$ADOPTOPENJDK" == "8" && "$TRAVIS_SCALA_VERSION" =~ ^2\.1[01234]\..*$ ]]; then
    true
  else
    false
  fi
}

if [[ "$SCALAJS_VERSION" != "" ]]; then
  projectPrefix="parserCombinatorsJS"
elif [[ "$SCALANATIVE_VERSION" != "" ]]; then
  projectPrefix="parserCombinatorsNative"
else
  projectPrefix="parserCombinators"
fi

verPat="[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9-]+)?"
tagPat="^v$verPat(#.*)?$"

if [[ "$TRAVIS_TAG" =~ $tagPat ]]; then
  releaseTask="ci-release"
  if ! isReleaseJob; then
    echo "Not releasing on Java $ADOPTOPENJDK with Scala $TRAVIS_SCALA_VERSION"
    exit 0
  fi
fi

# default is +publishSigned; we cross-build with travis jobs, not sbt's crossScalaVersions
export CI_RELEASE="$projectPrefix/publishSigned"
export CI_SNAPSHOT_RELEASE="$projectPrefix/publish"

# default is sonatypeBundleRelease, which closes and releases the staging repo
# see https://github.com/xerial/sbt-sonatype#commands
# for now, until we're confident in the new release scripts, just close the staging repo.
export CI_SONATYPE_RELEASE="; sonatypePrepare; sonatypeBundleUpload; sonatypeClose"

sbt clean $projectPrefix/test $projectPrefix/publishLocal $releaseTask
