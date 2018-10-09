if (System.getProperty("java.version").startsWith("1."))
  Seq()
else
  // override to version that works on Java 9,
  // see https://github.com/scala/sbt-scala-module/issues/35
  Seq(addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.9.3"))

addSbtPlugin("org.scala-lang.modules" % "sbt-scala-module" % "1.0.14")

val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).filter(_.nonEmpty).getOrElse("0.6.25")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

val scalaNativeVersion =
  Option(System.getenv("SCALANATIVE_VERSION")).filter(_.nonEmpty).getOrElse("0.3.8")

addSbtPlugin("org.scala-native" % "sbt-scala-native" % scalaNativeVersion)

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.0")

addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "0.6.0")
