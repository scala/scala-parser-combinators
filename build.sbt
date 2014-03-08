scalaModuleSettings

name                       := "scala-parser-combinators"

version                    := "1.0.0-SNAPSHOT"

scalaVersion               := "2.11.0-M8"

snapshotScalaBinaryVersion := "2.11.0-M8"

// important!! must come here (why?)
scalaModuleOsgiSettings

OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}")

// needed to fix classloader issues (see scala-xml#20)
fork in Test := true

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"
