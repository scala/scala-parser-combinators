import sbtcrossproject.CrossPlugin.autoImport.crossProject

lazy val parserCombinators = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .withoutSuffixFor(JVMPlatform).in(file("."))
  .settings(ScalaModulePlugin.scalaModuleSettings)
  .jvmSettings(ScalaModulePlugin.scalaModuleSettingsJVM)
  .settings(
    name := "scala-parser-combinators",

    scalaModuleMimaPreviousVersion := Some("1.1.0").filter(_ => System.getenv("SCALAJS_VERSION") != "1.0.0"),
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      import com.typesafe.tools.mima.core.ProblemFilters._
      Seq(
        exclude[IncompatibleSignatureProblem]("*"),

        // the following 3 are due to https://github.com/lightbend/mima/issues/388
        exclude[DirectMissingMethodProblem]("scala.util.parsing.json.JSON.numberParser"),
        exclude[DirectMissingMethodProblem]("scala.util.parsing.json.JSON.defaultNumberParser"),
        exclude[DirectMissingMethodProblem]("scala.util.parsing.json.JSON.keywordCache")
      )
    },

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
    },
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._, ProblemFilters._
      Seq(
        // these are safe to exclude because they're `private[combinator]`
        exclude[ReversedMissingMethodProblem]("scala.util.parsing.combinator.Parsers.Success"),
        exclude[ReversedMissingMethodProblem]("scala.util.parsing.combinator.Parsers.selectLastFailure"),
      )
    },
  )
  .jvmSettings(
    OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}"),
    libraryDependencies += "junit" % "junit" % "4.13" % Test,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test
  )
  .jsSettings(
    // Scala.js cannot run forked tests
    fork in Test := false
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .nativeSettings(
    skip in compile := System.getProperty("java.version").startsWith("1.6") || !scalaVersion.value.startsWith("2.11"),
    test := {},
    libraryDependencies := {
      if (!scalaVersion.value.startsWith("2.11"))
        libraryDependencies.value.filterNot(_.organization == "org.scala-native")
      else libraryDependencies.value
    }
  )
