import com.typesafe.tools.mima.plugin.{MimaPlugin, MimaKeys}

scalaModuleSettings

name                       := "scala-parser-combinators"

version                    := "1.0.2-SNAPSHOT"

scalaVersion               := "2.11.0"

snapshotScalaBinaryVersion := "2.11.0"

// important!! must come here (why?)
scalaModuleOsgiSettings

OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}")

// needed to fix classloader issues (see scala-xml#20)
fork in Test := true

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

MimaPlugin.mimaDefaultSettings

MimaKeys.previousArtifact := Some(organization.value % s"${name.value}_2.11" % "1.0.1")

// run mima during tests
test in Test := {
        MimaKeys.reportBinaryIssues.value
        (test in Test).value
}
