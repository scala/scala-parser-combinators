lazy val root = project.in(file("."))
  .aggregate(`scala-parser-combinatorsJVM`, `scala-parser-combinatorsJS`)
  .settings(publish := {}, publishLocal := {},
    crossScalaVersions := (crossScalaVersions in LocalProject("scala-parser-combinatorsJVM")).value,
    scalaVersion := crossScalaVersions.value.head)

lazy val `scala-parser-combinators` = crossProject.in(file(".")).
  settings(scalaModuleSettings: _*).
  settings(
    name := "scala-parser-combinators-root",
    scalaVersionsByJvm := {
      val v212 = "2.12.2"
      val v211 = "2.11.11"

      Map(
        6 -> List(v211 -> true),
        7 -> List(v211 -> false),
        8 -> List(v212 -> true, v211 -> false),
        9 -> List(v212 -> false, v211 -> false)
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
    )
  ).
  jvmSettings(
    // Mima uses the name of the jvm project in the artifactId
    // when resolving previous versions (so no "-jvm" project)
    name := "scala-parser-combinators"
  ).
  jsSettings(
    name := "scala-parser-combinators-js"
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
