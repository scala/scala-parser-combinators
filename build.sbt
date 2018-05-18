import ScalaModulePlugin._
import sbtcrossproject.crossProject

scalaVersionsByJvm in ThisBuild := {
  val v211 = "2.11.12"
  val v212 = "2.12.6"
  val v213 = "2.13.0-M4"

  Map(
    6 -> List(v211 -> true),
    7 -> List(v211 -> false),
    8 -> List(v212 -> true, v213 -> true, v211 -> false),
    9 -> List(v212 -> false, v213 -> false, v211 -> false)
  )
}

lazy val root = project.in(file("."))
  .aggregate(`scala-parser-combinatorsJS`, `scala-parser-combinatorsJVM`)
  .settings(disablePublishing)

lazy val `scala-parser-combinators` = crossProject(JSPlatform, JVMPlatform).in(file(".")).
  settings(scalaModuleSettings: _*).
  jvmSettings(scalaModuleSettingsJVM).
  settings(
    name := "scala-parser-combinators",
    version := "1.1.1-SNAPSHOT",
    mimaPreviousVersion := Some("1.1.0"),

    apiMappings += (scalaInstance.value.libraryJar ->
        url(s"https://www.scala-lang.org/api/${scalaVersion.value}/")),

    scalacOptions in (Compile, doc) ++= Seq(
      "-diagrams",
      "-doc-source-url",
      s"https://github.com/scala/scala-parser-combinators/tree/v${version.value}€{FILE_PATH}.scala",
      "-sourcepath",
      (baseDirectory in LocalRootProject).value.absolutePath,
      "-doc-title",
      "Scala Parser Combinators",
      "-doc-version",
      version.value
    ),
    unmanagedSourceDirectories in Compile ++= {
      (unmanagedSourceDirectories in Compile).value.map { dir =>
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 13)) => file(dir.getPath ++ "-2.13")
          case _             => file(dir.getPath ++ "-2.11-2.12")
        }
      }
    }
  ).
  jvmSettings(
    OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}"),
    libraryDependencies += "junit" % "junit" % "4.12" % "test",
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  ).
  jsSettings(
    // Scala.js cannot run forked tests
    fork in Test := false
  ).
  jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))

lazy val `scala-parser-combinatorsJVM` = `scala-parser-combinators`.jvm
lazy val `scala-parser-combinatorsJS` = `scala-parser-combinators`.js
