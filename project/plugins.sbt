val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).filter(_.nonEmpty).getOrElse("1.4.0")

val scalaNativeVersion =
  Option(System.getenv("SCALANATIVE_VERSION")).filter(_.nonEmpty).getOrElse("0.4.0")

addSbtPlugin("org.scala-lang.modules" % "sbt-scala-module" % "2.2.4")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.0.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % scalaNativeVersion)

addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.5.2")
