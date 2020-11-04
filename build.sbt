lazy val root = project.in(file("."))
  .aggregate(parserCombinatorsJVM, parserCombinatorsJS, parserCombinatorsNative)
  .settings(
    publish / skip := true,
  )

lazy val parserCombinators = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .settings(
    ScalaModulePlugin.scalaModuleSettings,
    name := "scala-parser-combinators",
    scalaModuleMimaPreviousVersion := None,  // until we publish 1.2.0

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
    // don't run Dottydoc, it errors and isn't needed anyway
    Compile / doc / sources := (if (isDotty.value) Seq() else (Compile / doc/ sources).value),
    Compile / packageDoc / publishArtifact := !isDotty.value,
    Compile / doc / scalacOptions ++= {
      if (isDotty.value)
        Seq()
      else
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
    },
    Compile / unmanagedSourceDirectories ++= {
      (Compile / unmanagedSourceDirectories).value.map { dir =>
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 13)) => file(dir.getPath ++ "-2.13+")
          case Some((0, _))  => file(dir.getPath ++ "-2.13+")
          case _             => file(dir.getPath ++ "-2.13-")
        }
      }
    }
  )
  .jvmSettings(
    ScalaModulePlugin.scalaModuleOsgiSettings,
    OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}"),
    libraryDependencies += "junit" % "junit" % "4.13.1" % Test,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test
  )
  .jsSettings(
    crossScalaVersions -= "0.27.0-RC1",
    // Scala.js cannot run forked tests
    Test / fork := false
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .nativeSettings(
    compile / skip := System.getProperty("java.version").startsWith("1.6") || !scalaVersion.value.startsWith("2.11"),
    test := {},
    libraryDependencies := {
      if (!scalaVersion.value.startsWith("2.11"))
        libraryDependencies.value.filterNot(_.organization == "org.scala-native")
      else libraryDependencies.value
    }
  )

lazy val parserCombinatorsJVM    = parserCombinators.jvm
lazy val parserCombinatorsJS     = parserCombinators.js
lazy val parserCombinatorsNative = parserCombinators.native
