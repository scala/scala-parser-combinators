import com.typesafe.tools.mima.plugin.{MimaPlugin, MimaKeys}
import com.typesafe.tools.mima.core.{ProblemFilters, MissingMethodProblem}

scalaModuleSettings

name                       := "scala-parser-combinators"

version                    := "1.0.3-SNAPSHOT"

scalaVersion               := "2.11.2"

snapshotScalaBinaryVersion := "2.11"

// important!! must come here (why?)
scalaModuleOsgiSettings

OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}")

// needed to fix classloader issues (see scala-xml#20)
fork in Test := true

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

MimaPlugin.mimaDefaultSettings

MimaKeys.previousArtifact := Some(organization.value % s"${name.value}_2.11" % "1.0.2")

// run mima during tests
test in Test := {
        MimaKeys.reportBinaryIssues.value
        (test in Test).value
}

MimaKeys.binaryIssueFilters += ProblemFilters.exclude[MissingMethodProblem]("scala.util.parsing.combinator.RegexParsers.scala$util$parsing$combinator$RegexParsers$$super$err")
