import ScalaModulePlugin._
import sbtcrossproject.{crossProject, CrossType}

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
  .aggregate(`scala-parser-combinatorsJS`, `scala-parser-combinatorsJVM`, `scala-parser-combinatorsNative`)
  .settings(disablePublishing)

lazy val `scala-parser-combinators` = crossProject(JSPlatform, JVMPlatform, NativePlatform).in(file(".")).
  settings(scalaModuleSettings: _*).
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
  jvmSettings(scalaModuleSettingsJVM).
  jvmSettings(
    // Mima uses the name of the jvm project in the artifactId
    // when resolving previous versions (so no "-jvm" project)
    name := "scala-parser-combinators",
    OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}"),
    libraryDependencies += "junit" % "junit" % "4.12" % "test",
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  ).
  jsSettings(
    name := "scala-parser-combinators-js",
    // Scala.js cannot run forked tests
    fork in Test := false
  ).
  jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin)).
  nativeSettings(
    name := "scala-parser-combinators-native",
    scalaVersion := "2.11.11",
    skip in compile := System.getProperty("java.version").startsWith("1.6"),
    test := {},
    libraryDependencies := {
      if (!scalaVersion.value.startsWith("2.11"))
        libraryDependencies.value.filterNot(_.organization == "org.scala-native")
      else libraryDependencies.value
    }
  )

lazy val `scala-parser-combinatorsJVM` = `scala-parser-combinators`.jvm
lazy val `scala-parser-combinatorsJS` = `scala-parser-combinators`.js
lazy val `scala-parser-combinatorsNative` = `scala-parser-combinators`.native
