#!/bin/bash

set -e

# Builds of tagged revisions are published to sonatype staging.

# Travis runs a build on new revisions, including on new tags.
# Builds for a tag have TRAVIS_TAG defined, which we use for identifying tagged builds.
# Checking the local git clone would not work because git on travis does not fetch tags.

# The version number to be published is extracted from the tag, e.g., v1.2.3 publishes
# version 1.2.3 using all Scala versions in the travis matrix where
# [ "$RELEASE_COMBO" = "true" ].

# In order to build a previously released version against a new (binary incompatible) Scala release,
# a new commit that modifies (and prunes) the Scala versions in .travis.yml needs to be added on top
# of the existing tag. Then a new tag can be created for that commit, e.g., `v1.2.3#2.13.0-M5`.
# Everything after the `#` in the tag name is ignored.

if [[ "$SCALANATIVE_VERSION" != "" ]]; then
  if [[ "$TRAVIS_JDK_VERSION" == "oraclejdk8" && "$TRAVIS_SCALA_VERSION" =~ 2\.11\..* ]]; then
    RELEASE_COMBO=true;
  fi
elif [[ "$TRAVIS_JDK_VERSION" == "oraclejdk8" ]]; then
  RELEASE_COMBO=true;
fi

if ! [ "$SCALAJS_VERSION" == "" ]; then
  projectPrefix="scala-parser-combinatorsJS"
elif ! [ "$SCALANATIVE_VERSION" == "" ]; then
  projectPrefix="scala-parser-combinatorsNative"
else
  projectPrefix="scala-parser-combinators"
fi

verPat="[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9-]+)?"
tagPat="^v$verPat(#$verPat#[0-9]+)?$"

if [[ "$TRAVIS_TAG" =~ $tagPat ]]; then
  tagVer=${TRAVIS_TAG}
  tagVer=${tagVer#v}   # Remove `v` at beginning.
  tagVer=${tagVer%%#*} # Remove anything after `#`.
  publishVersion='set every version := "'$tagVer'"'

  if [ "$RELEASE_COMBO" = "true" ]; then
    currentJvmVer=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | sed 's/^1\.//' | sed 's/[^0-9].*//')
    echo "Releasing $tagVer with Scala $TRAVIS_SCALA_VERSION on Java version $currentJvmVer."

    publishTask="$projectPrefix/publishSigned"

    cat admin/gpg.sbt >> project/plugins.sbt
    cp admin/publish-settings.sbt .

    # Copied from the output of genKeyPair.sh
    K=$encrypted_5e972ec514e2_key
    IV=$encrypted_5e972ec514e2_iv

    openssl aes-256-cbc -K $K -iv $IV -in admin/secring.asc.enc -out admin/secring.asc -d
  fi
fi

sbt "++$TRAVIS_SCALA_VERSION" "$publishVersion" "$projectPrefix/update"  "$projectPrefix/compile" "$projectPrefix/test" "$projectPrefix/publishLocal" "$publishTask"
