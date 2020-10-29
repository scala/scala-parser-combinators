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

    Compile / doc / scalacOptions ++= {
      if (isDotty.value)
        Seq("-language:Scala2")
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
