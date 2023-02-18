ThisBuild / licenses += (("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))
ThisBuild / startYear := Some(2004)

val commonSettings = Seq(
  versionScheme := Some("early-semver"),
  // next version will bump minor (because we dropped Scala 2.11 and upgraded
  // Scala.js and Scala Native); we could go back to BinaryAndSourceCompatible
  // once that's done
  versionPolicyIntention := Compatibility.BinaryCompatible,
  crossScalaVersions := Seq("2.13.10", "2.12.17", "3.2.2"),
  scalaVersion := crossScalaVersions.value.head,
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

    libraryDependencies += "junit" % "junit" % "4.13.2" % Test,
    libraryDependencies += "com.github.sbt" % "junit-interface" % "0.13.3" % Test,

    apiMappings ++= scalaInstance.value.libraryJars.collect {
      case file if file.getName.startsWith("scala-library") && file.getName.endsWith(".jar") =>
        file -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/")
    }.toMap,

    // go nearly warning-free, but only on 2.13, it's too hard across all versions
    Compile / scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) => Seq("-Werror", "-Wunused",
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
    }
  )
  .jvmSettings(
    ScalaModulePlugin.scalaModuleOsgiSettings,
    OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}"),
  )
  .jsSettings(
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
  .nativeEnablePlugins(ScalaNativeJUnitPlugin)
  .nativeSettings(
    versionPolicyCheck / skip := true,
    versionCheck       / skip := true,
    Test / fork := false,
  )

lazy val parserCombinatorsJVM    = parserCombinators.jvm
lazy val parserCombinatorsJS     = parserCombinators.js
lazy val parserCombinatorsNative = parserCombinators.native
