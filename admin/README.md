## Tag Driven Releasing

### Initial setup for the repository

To configure tag driven releases from Travis CI.

  1. Generate a key pair for this repository with `./admin/genKeyPair.sh`.
     Edit `.travis.yml` and `admin/build.sh` as prompted.
  1. Publish the public key to https://pgp.mit.edu
  1. Store other secrets as encrypted environment variables with `./admin/encryptEnvVars.sh`.
     Edit `.travis.yml` as prompted.
  1. Edit `.travis.yml` to use `./admin/build.sh` as the build script,
     and edit that script to use the tasks required for this project.
     Ensure that `RELEASE_COMBO` is `true` for build matrix combinations
     that should be released to sonatype (when building a tag).

It is important to add comments in `.travis.yml` to identify the name
of each environment variable encoded in a `secure` section.

After these steps, your `.travis.yml` should contain config of the form:

```
language: scala

env:
  global:
    # PGP_PASSPHRASE
    - secure: "XXXXXX"
    # SONA_USER
    - secure: "XXXXXX"
    # SONA_PASS
    - secure: "XXXXXX"

script:
  - admin/build.sh

jdk:
  - oraclejdk8
  - openjdk11

notifications:
  email:
    - a@b.com
```

If Sonatype credentials change in the future, step 3 can be repeated
without generating a new key.

### Testing

  1. Follow the release process below to create a dummy release (e.g., `v0.1.0-TEST1`).
     Confirm that the release was staged to Sonatype but do not release it to Maven
     central. Instead, drop the staging repository.

### Performing a release

  1. Create a GitHub "Release" with a corresponding tag (e.g., `v0.1.1`) via the GitHub
     web interface.
  1. The release will be published using the Scala and JVM version combinations specified
     in the travis build matrix where `[ "$RELEASE_COMBO" = "true" ]`.
     - If you need to release against a different Scala version, create a new commit that modifies
       `.travis.yml` and push a new tag, e.g., `v1.2.3#2.13.0-M5`. The suffix after `#` is ignored.
  1. Travis CI will schedule a build for this release. Review the build logs.
  1. Log into https://oss.sonatype.org/ and identify the staging repository.
  1. Sanity check its contents.
  1. Release staging repository to Maven and send out release announcement.
