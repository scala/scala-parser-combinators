organization := "org.scala-lang.modules"

name := "scala-parser-combinators"

version := "1.0-RC2"

scalaVersion := "2.11.0-M5"

// NOTE: not necessarily equal to scalaVersion
// (e.g., during PR validation, we override scalaVersion to validate,
// but don't rebuild scalacheck, so we don't want to rewire that dependency)
scalaBinaryVersion := "2.11.0-M5"

// don't use for doc scope, scaladoc warnings are not to be reckoned with
scalacOptions in compile ++= Seq("-optimize", "-Xfatal-warnings", "-feature", "-deprecation", "-unchecked", "-Xlint")


// Generate $name.properties to store our version as well as the scala version used to build
resourceGenerators in Compile <+= Def.task {
  val props = new java.util.Properties
  props.put("version.number", version.value)
  props.put("scala.version.number", scalaVersion.value)
  props.put("scala.binary.version.number", scalaBinaryVersion.value)
  val file = (resourceManaged in Compile).value / s"${name.value}.properties"
  IO.write(props, null, file)
  Seq(file)
}

mappings in (Compile, packageBin) += {
   (baseDirectory.value / s"${name.value}.properties") -> s"${name.value}.properties"
}


// maven publishing
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>http://www.scala-lang.org/</url>
  <inceptionYear>2002</inceptionYear>
  <licenses>
    <license>
        <distribution>repo</distribution>
        <name>BSD 3-Clause</name>
        <url>https://github.com/scala/{name.value}/blob/master/LICENSE.md</url>
    </license>
   </licenses>
  <scm>
    <connection>scm:git:git://github.com/scala/{name.value}.git</connection>
    <url>https://github.com/scala/{name.value}</url>
  </scm>
  <issueManagement>
    <system>JIRA</system>
    <url>https://issues.scala-lang.org/</url>
  </issueManagement>
  <developers>
    <developer>
      <id>epfl</id>
      <name>EPFL</name>
    </developer>
    <developer>
      <id>Typesafe</id>
      <name>Typesafe, Inc.</name>
    </developer>
  </developers>
)

// for testing with partest
libraryDependencies += "org.scala-lang.modules" %% "scala-partest-interface" % "0.2" % "test"

// the actual partest the interface calls into -- must be binary version close enough to ours
// so that it can link to the compiler/lib we're using (testing)
libraryDependencies += "org.scala-lang.modules" %% "scala-partest" % "1.0-RC5" % "test"

// necessary for partest -- see comments in its build.sbt
conflictWarning ~= { _.copy(failOnConflict = false) }

fork in Test := true

javaOptions in Test += "-Xmx1G"

testFrameworks += new TestFramework("scala.tools.partest.Framework")

definedTests in Test += (
  new sbt.TestDefinition(
    "partest",
    // marker fingerprint since there are no test classes
    // to be discovered by sbt:
    new sbt.testing.AnnotatedFingerprint {
      def isModule = true
      def annotationName = "partest"
    }, true, Array())
  )


// TODO: mima
// import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
// import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact
// previousArtifact := Some(organization.value %% name.value % binaryReferenceVersion.value)
