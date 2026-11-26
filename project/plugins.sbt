addSbtPlugin("org.scala-lang.modules" % "sbt-scala-module" % "3.4.1")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.2.1") // drop once `sbt-scala-module` brings in 1.2.0

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.4.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.23.0")

addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.4.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.13")
