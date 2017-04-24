## Tag Driven Releasing

Copied from https://github.com/scala/scala-java8-compat/commit/4a6cfc97cd95227b86650410e1b632e5ff79335b.

### Background Reading

  - http://docs.travis-ci.com/user/environment-variables/
  - http://docs.travis-ci.com/user/encryption-keys/
  - http://docs.travis-ci.com/user/encrypting-files/

### Initial setup for the repository

To configure tag driven releases from Travis CI.

  1. Generate a key pair for this repository with `./admin/genKeyPair.sh`.
     Edit `.travis.yml` and `admin/build.sh` as prompted.
  2. Publish the public key to https://pgp.mit.edu
  3. Store other secrets as encrypted environment variables with `admin/encryptEnvVars.sh`.
     Edit `.travis.yml` as prompted.
  4. Edit `.travis.yml` to use `./admin/build.sh` as the build script,
     and edit that script to use the tasks required for this project.
  5. Edit `build.sbt`'s `scalaVersionsByJvm` to select which Scala and JDK will
     be used for publishing.

It is important to add comments in .travis.yml to identify the name
of each environment variable encoded in a `:secure` section.

After all of these steps, your .travis.yml should contain config of the
form:

	language: scala
	env:
	  global:
	    # PGP_PASSPHRASE
	    - secure: "XXXXXX"
	    # SONA_USER
	    - secure: "XXXXXX"
	    # SONA_PASS
	    - secure: "XXXXXX"
	script: admin/build.sh
        jdk:
            - openjdk6
            - oraclejdk8

If Sonatype credentials change in the future, step 3 can be repeated
without generating a new key.

### Testing

  1. Follow the release process below to create a dummy release (e.g. 0.1.0-TEST1).
     Confirm that the release was staged to Sonatype but do not release it to Maven
     central. Instead, drop the staging repository.

### Performing a release

  1. Create a GitHub "Release" (with a corresponding tag) via the GitHub
     web interface.
  2. The release will be published using the Scala and JVM version combinations specified
     in `scalaVersionsByJvm` in `build.sbt`.
     - If you need to release against a different Scala version, include the Scala version
       and the JVM version to use in the tag name, separated by `#`s (e.g., `v0.1.1#2.13.0-M1#8`).
       Note that the JVM version needs to be listed in `.travis.yml` for the build to run.
  3. Travis CI will schedule a build for this release. Review the build logs.
  4. Log into https://oss.sonatype.org/ and identify the staging repository.
  5. Sanity check its contents
  6. Release staging repository to Maven and send out release announcement.

