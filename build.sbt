import ScalaModulePlugin._
import sbtcrossproject.crossProject

crossScalaVersions in ThisBuild := List("2.12.8", "2.11.12", "2.13.0")

lazy val root = project.in(file("."))
  .aggregate(`scala-parser-combinatorsJS`, `scala-parser-combinatorsJVM`, `scala-parser-combinatorsNative`)
  .settings(disablePublishing)

lazy val `scala-parser-combinators` = crossProject(JSPlatform, JVMPlatform, NativePlatform).
  withoutSuffixFor(JVMPlatform).in(file(".")).
  settings(scalaModuleSettings: _*).
  jvmSettings(scalaModuleSettingsJVM).
  settings(
    name := "scala-parser-combinators",
    version := "1.1.2-SNAPSHOT",
    mimaPreviousVersion := Some("1.1.0").filter(_ => System.getenv("SCALAJS_VERSION") != "1.0.0-M8"),

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
  jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin)).
  nativeSettings(
    skip in compile := System.getProperty("java.version").startsWith("1.6") || !scalaVersion.value.startsWith("2.11"),
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
