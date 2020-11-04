addSbtPlugin("org.scala-lang.modules" % "sbt-scala-module" % "2.2.3")

val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).filter(_.nonEmpty).getOrElse("1.3.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.4.5")
