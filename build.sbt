import sbtcrossproject.CrossPlugin.autoImport.crossProject

lazy val parserCombinators = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .withoutSuffixFor(JVMPlatform).in(file("."))
  .settings(ScalaModulePlugin.scalaModuleSettings)
  .jvmSettings(ScalaModulePlugin.scalaModuleSettingsJVM)
  .settings(
    name := "scala-parser-combinators",
    scalaModuleMimaPreviousVersion := None,

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
          case Some((2, 13)) => file(dir.getPath ++ "-2.13+")
          case Some((0, _))  => file(dir.getPath ++ "-2.13+")
          case _             => file(dir.getPath ++ "-2.13-")
        }
      }
    }
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
