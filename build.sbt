import com.typesafe.tools.mima.plugin.{MimaPlugin, MimaKeys}

scalaModuleSettings

name                       := "scala-parser-combinators"

version                    := "1.0.5-SNAPSHOT"

crossScalaVersions in ThisBuild := {
  val javaVersion = System.getProperty("java.version")
  val isJDK6Or7 =
    javaVersion.startsWith("1.6.") || javaVersion.startsWith("1.7.")
  if (isJDK6Or7)
    Seq("2.11.7")
  else
    Seq("2.11.7", "2.12.0-M3")
}

// important!! must come here (why?)
scalaModuleOsgiSettings

OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}")

// needed to fix classloader issues (see scala-xml#20)
fork in Test := true

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

mimaPreviousVersion := Some("1.0.2")
