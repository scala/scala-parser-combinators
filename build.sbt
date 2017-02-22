scalaVersion in ThisBuild := crossScalaVersions.value.head

crossScalaVersions in ThisBuild := {
  val v211 = List("2.11.8")
  val v212 = List("2.12.1")

  val javaVersion = System.getProperty("java.version")
  val isTravisPublishing = !util.Properties.envOrElse("TRAVIS_TAG", "").trim.isEmpty

  if (isTravisPublishing) {
    if (javaVersion.startsWith("1.6.")) v211
    else if (javaVersion.startsWith("1.8.")) v212
    else Nil
  } else if (javaVersion.startsWith("1.6.") || javaVersion.startsWith("1.7.")) {
    v211
  } else if (javaVersion.startsWith("1.8.") || javaVersion.startsWith("9")) {
    v211 ++ v212
  } else {
    sys.error(s"Unsupported java version: $javaVersion.")
  }
}

lazy val `scala-parser-combinators` = crossProject.in(file(".")).
  settings(scalaModuleSettings: _*).
  settings(
    name := "scala-parser-combinators-root",
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
    // Mima uses the name of the jvm project in the artifactId
    // when resolving previous versions (so no "-jvm" project)
    name := "scala-parser-combinators"
  ).
  jsSettings(
    name := "scala-parser-combinators-js",
    scalaJSUseRhino := true
  ).
  settings(
    moduleName         := "scala-parser-combinators",
    version            := "1.0.6-SNAPSHOT"
  ).
  jvmSettings(
    // important!! must come here (why?)
    scalaModuleOsgiSettings: _*
  ).
  jvmSettings(
    OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}"),

    // needed to fix classloader issues (see scala-xml#20)
    fork in Test := true
  ).
  jsSettings(
    // Scala.js cannot run forked tests
    fork in Test := false
  ).
  jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin)).
  jvmSettings(
    libraryDependencies += "junit" % "junit" % "4.12" % "test",
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  ).
  jvmSettings(
    mimaPreviousVersion := Some("1.0.4")
  )

lazy val `scala-parser-combinatorsJVM` = `scala-parser-combinators`.jvm
lazy val `scala-parser-combinatorsJS` = `scala-parser-combinators`.js
