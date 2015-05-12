import com.typesafe.tools.mima.plugin.{MimaPlugin, MimaKeys}

scalaModuleSettings

name                       := "scala-parser-combinators"

version                    := "1.0.5-SNAPSHOT"

scalaVersion               := crossScalaVersions.value.head

crossScalaVersions         := Seq("2.11.6", "2.12.0-M1")

// important!! must come here (why?)
scalaModuleOsgiSettings

OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}")

// needed to fix classloader issues (see scala-xml#20)
fork in Test := true

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

mimaPreviousVersion := Some("1.0.2")
