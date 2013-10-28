organization := "org.scala-lang.modules"

name := "scala-parser-combinators"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.0-M6"

// NOTE: not necessarily equal to scalaVersion
// (e.g., during PR validation, we override scalaVersion to validate,
// but don't rebuild scalacheck, so we don't want to rewire that dependency)
scalaBinaryVersion := "2.11.0-M6"

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

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

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

libraryDependencies ++= Seq("junit" % "junit" % "4.11" % "test", "com.novocode" % "junit-interface" % "0.10" % "test")

// default value must be set here
TestKeys.includeTestDependencies := true

// default
TestKeys.partestVersion := "1.0.0-RC6"

// the actual partest the interface calls into -- must be binary version close enough to ours
// so that it can link to the compiler/lib we're using (testing)
// NOTE: not sure why, but the order matters (maybe due to the binary version conflicts for xml/parser combinators pulled in for scaladoc?)
libraryDependencies ++= (
  if (TestKeys.includeTestDependencies.value) {
    /**
     * Exclude all transitive dependencies of partest that include scala-parser-combinators.
     * This way we avoid having two (or more) versions of scala-parser-combinators on a classpath.
     * See this comment which describes the same issue for scala-xml
     * https://github.com/scala/scala-xml/pull/6#issuecomment-26614894
     *
     * Note that we are using ModuleID.exclude instead of more flexible ModuleID.excludeAll
     * (describe here: http://www.scala-sbt.org/release/docs/Detailed-Topics/Library-Management#exclude-transitive-dependencies)
     * because only plain excludes are incorporated in generated pom.xml. There are two ways
     * to address this problem:
     *
     *   1. Figure out how to depend on partest in non-transitive way: not include that dependency
     *      in generated pom.xml for scala-parser-combinators.
     *   2. Declare dependencies in partest as provided so they are not includeded transitively.
     */
    def excludeScalaXml(dep: ModuleID): ModuleID =
      dep.exclude("org.scala-lang.modules", "scala-parser-combinators_2.11.0-M4").
      exclude("org.scala-lang.modules", "scala-parser-combinators_2.11.0-M5").
      exclude("org.scala-lang.modules", "scala-parser-combinators_2.11.0-M6")
    Seq("org.scala-lang.modules" % "scala-partest-interface_2.11.0-M5" % "0.2"                         % "test" intransitive,
        "org.scala-lang.modules" % "scala-partest_2.11.0-M5"           % TestKeys.partestVersion.value % "test" intransitive,
        // diffutils is needed by partest
        "com.googlecode.java-diff-utils" % "diffutils"      % "1.3.0" % "test",
        "org.scala-lang" % "scala-compiler" % scalaVersion.value % "test").
      map(excludeScalaXml)
  }
  else Seq.empty
)

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

osgiSettings

val osgiVersion = version(_.replace('-', '.'))

OsgiKeys.bundleSymbolicName := s"${organization.value}.${name.value}"

OsgiKeys.bundleVersion := osgiVersion.value

OsgiKeys.exportPackage := Seq(s"scala.util.parsing.*;version=${version.value}")

// Sources should also have a nice MANIFEST file
packageOptions in packageSrc := Seq(Package.ManifestAttributes(
                      ("Bundle-SymbolicName", s"${organization.value}.${name.value}.source"),
                      ("Bundle-Name", s"${name.value} sources"),
                      ("Bundle-Version", osgiVersion.value),
                      ("Eclipse-SourceBundle", s"""${organization.value}.${name.value};version="${osgiVersion.value}";roots:="."""")
                  ))


// TODO: mima
// import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
// import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact
// previousArtifact := Some(organization.value %% name.value % binaryReferenceVersion.value)
