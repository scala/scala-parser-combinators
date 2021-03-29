lazy val root = project.in(file("."))
  .aggregate(parserCombinatorsJVM, parserCombinatorsJS, parserCombinatorsNative)
  .settings(
    publish / skip := true,
    ThisBuild / versionScheme := Some("early-semver"),
    ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible
  )

lazy val parserCombinators = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .settings(
    ScalaModulePlugin.scalaModuleSettings,
    name := "scala-parser-combinators",
    scalaModuleMimaPreviousVersion := (CrossVersion.partialVersion(scalaVersion.value) match {
      // pending resolution of https://github.com/scalacenter/sbt-version-policy/issues/62
      case Some((3, _)) => None
      case _            => Some("1.2.0-M2")
    }),

    libraryDependencies += "junit" % "junit" % "4.13.2" % Test,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
    // so we can `@nowarn` in test code, but only in test code, so the dependency
    // doesn't leak downstream
    libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.3" % Test,

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
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .nativeSettings(
    compile / skip := System.getProperty("java.version").startsWith("1.6") || !scalaVersion.value.startsWith("3"),
    test := {},
    libraryDependencies := {
      if (!scalaVersion.value.startsWith("3"))
        libraryDependencies.value.filterNot(_.organization == "org.scala-native")
      else libraryDependencies.value
    }
  )

lazy val parserCombinatorsJVM    = parserCombinators.jvm
lazy val parserCombinatorsJS     = parserCombinators.js
lazy val parserCombinatorsNative = parserCombinators.native
