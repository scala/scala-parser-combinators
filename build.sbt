// in the interest of reducing diffs between the 1.1.x branch and the
// main (2.x branch), there are some Scala 3 references in here that
// aren't affecting anything

ThisBuild / licenses += (("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))
ThisBuild / startYear := Some(2004)

// I thought we could declare these in `ThisBuild` scope but no :-/
val commonSettings = Seq(
  versionScheme := Some("early-semver"),
  versionPolicyIntention := {
    if (scalaVersion.value.startsWith("3"))
      Compatibility.None
    else
      Compatibility.BinaryAndSourceCompatible
    }
)

lazy val root = project.in(file("."))
  .aggregate(parserCombinatorsJVM, parserCombinatorsJS, parserCombinatorsNative)
  .settings(
    commonSettings,
    publish / skip := true,
  )

lazy val parserCombinators = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .settings(
    ScalaModulePlugin.scalaModuleSettings,
    commonSettings,
    name := "scala-parser-combinators",
    scalaModuleAutomaticModuleName := Some("scala.util.parsing"),

    crossScalaVersions := Seq("2.13.8", "2.12.15", "2.11.12"),
    scalaVersion := crossScalaVersions.value.head,

    libraryDependencies += "junit" % "junit" % "4.13.2" % Test,
    libraryDependencies += "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
    // so we can `@nowarn` in test code, but only in test code, so the dependency
    // doesn't leak downstream. can be dropped when we drop 2.11 from the crossbuild
    libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % "2.7.0" % Test,

    apiMappings ++= scalaInstance.value.libraryJars.collect {
      case file if file.getName.startsWith("scala-library") && file.getName.endsWith(".jar") =>
        file -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/")
    }.toMap,

    // go nearly warning-free, but only on 2.13, it's too hard across all versions
    Compile / scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) => Seq("-Werror",
        // ideally we'd do something about this. `^?` is the responsible method
        "-Wconf:site=scala.util.parsing.combinator.Parsers.*&cat=lint-multiarg-infix:i",
        // not sure what resolving this would look like? didn't think about it too hard
        "-Wconf:site=scala.util.parsing.combinator.lexical.StdLexical.*&cat=other-match-analysis:i",
      )
      case _ => Seq()
    }),
    Compile / doc / scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) => Seq(
        // it isn't able to link to [[java.lang.NoSuchMethodError]]
        // scala-xml doesn't have this problem, I tried copying their apiMappings stuff
        // and that didn't help, I'm mystified why :-/
        """-Wconf:msg=Could not find any member to link for*:i""",
      )
      case _ => Seq()
    }),
    Compile / doc / scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) =>
        Seq()  // TODO see what flags might be desirable to pass to Scala 3's Scaladoc
      case _ =>
        Seq(
          "-diagrams",
          "-doc-source-url",
          s"https://github.com/scala/scala-parser-combinators/tree/v${version.value}€{FILE_PATH}.scala",
          "-sourcepath",
          (LocalRootProject / baseDirectory).value.absolutePath,
          "-doc-title",
          "Scala Parser Combinators",
          "-doc-version",
          version.value
        )
    }),
    Compile / unmanagedSourceDirectories ++= {
      (Compile / unmanagedSourceDirectories).value.map { dir =>
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 13)) => file(dir.getPath ++ "-2.13+")
          case Some((3, _))  => file(dir.getPath ++ "-2.13+")
          case _             => file(dir.getPath ++ "-2.13-")
        }
      }
    },
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._, ProblemFilters._
      Seq(
        // these are safe to exclude because they're `private[parsing]`
        exclude[DirectMissingMethodProblem  ]("scala.util.parsing.combinator.Parsers.Success"),
        exclude[ReversedMissingMethodProblem]("scala.util.parsing.combinator.Parsers.Success"),
        exclude[DirectMissingMethodProblem  ]("scala.util.parsing.combinator.lexical.Lexical.Success"),
        exclude[ReversedMissingMethodProblem]("scala.util.parsing.combinator.lexical.Lexical.Success"),
        exclude[DirectMissingMethodProblem  ]("scala.util.parsing.combinator.Parsers.selectLastFailure"),
        exclude[ReversedMissingMethodProblem]("scala.util.parsing.combinator.Parsers.selectLastFailure"),
        exclude[DirectMissingMethodProblem  ]("scala.util.parsing.combinator.lexical.Lexical.selectLastFailure"),
        exclude[ReversedMissingMethodProblem]("scala.util.parsing.combinator.lexical.Lexical.selectLastFailure"),
        exclude[DirectMissingMethodProblem  ]("scala.util.parsing.combinator.syntactical.StandardTokenParsers.Success"),
        exclude[ReversedMissingMethodProblem]("scala.util.parsing.combinator.syntactical.StandardTokenParsers.Success"),
        exclude[DirectMissingMethodProblem  ]("scala.util.parsing.combinator.syntactical.StandardTokenParsers.selectLastFailure"),
        exclude[ReversedMissingMethodProblem]("scala.util.parsing.combinator.syntactical.StandardTokenParsers.selectLastFailure"),
        exclude[DirectMissingMethodProblem  ]("scala.util.parsing.json.Parser.Success"),
        exclude[ReversedMissingMethodProblem]("scala.util.parsing.json.Parser.Success"),
        exclude[DirectMissingMethodProblem  ]("scala.util.parsing.json.Parser.selectLastFailure"),
        exclude[ReversedMissingMethodProblem]("scala.util.parsing.json.Parser.selectLastFailure"),
      )
    },
  )
  .jvmSettings(
    ScalaModulePlugin.scalaModuleOsgiSettings,
    OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}"),
  )
  .jsSettings(
    // this is failing because between 1.1.2 and 1.1.3 we upgraded Scala.js from
    // 1.0.0 to 1.10.0. I think we can re-enable this once we've published 1.1.3?
    versionPolicyCheck / skip := true,
    // mystified why https://github.com/scala-js/scala-js/issues/635 would be rearing its head,
    // but only on sbt 1.4 + 2.13 and only in Test config?! WEIRD
    Test / doc / scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) => Seq("-Wconf:msg=dropping dependency on node with no phase object*:i")
      case _ => Seq()
    }),
    // Scala.js cannot run forked tests
    Test / fork := false
  )
  .jsEnablePlugins(ScalaJSJUnitPlugin)
  .nativeSettings(
    versionPolicyCheck / skip := true,
    versionCheck       / skip := true,
    Test / fork := false,
    libraryDependencies :=
        libraryDependencies.value.filterNot(_.organization == "junit") :+ "org.scala-native" %%% "junit-runtime" % "0.4.4",
    addCompilerPlugin("org.scala-native" % "junit-plugin" % "0.4.4" cross CrossVersion.full)
  )

lazy val parserCombinatorsJVM    = parserCombinators.jvm
lazy val parserCombinatorsJS     = parserCombinators.js
lazy val parserCombinatorsNative = parserCombinators.native
