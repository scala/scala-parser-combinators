// only added when publishing:
addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")

/* There's a companion sensitive.sbt, which was created like this:

1. in an sbt shell when sbt-gpg is loaded, create pgp key in admin/:

 set pgpReadOnly := false
 pgp-cmd gen-key // use $passPhrase
 pgp-cmd send-key <keyIdUsingTabCompletion> hkp://keyserver.ubuntu.com

2. create sensitive.sbt with contents:

pgpPassphrase := Some($passPhrase.toArray)

pgpPublicRing := file("admin/pubring.asc")

pgpSecretRing := file("admin/secring.asc")

credentials   += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", $sonaUser, $sonaPass)
*/
