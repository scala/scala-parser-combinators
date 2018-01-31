import ScalaModulePlugin._

scalaVersionsByJvm in ThisBuild := {
  val v211 = "2.11.11"
  val v212 = "2.12.3"
  val v213 = "2.13.0-M2"

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

lazy val `scala-parser-combinators` = crossProject.in(file(".")).
  settings(scalaModuleSettings: _*).
  jvmSettings(scalaModuleSettingsJVM).
  settings(
    name := "scala-parser-combinators",
    version := "1.0.7-SNAPSHOT",
    mimaPreviousVersion := Some("1.0.5"),

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
    )
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
