object VersionKeys {
  import sbt.settingKey

  val snapshotScalaBinaryVersion = settingKey[String]("The Scala binary version to use when building against Scala SNAPSHOT.")

  def deriveBinaryVersion(sv: String, snapshotScalaBinaryVersion: String) = sv match {
    case snap_211 if snap_211.startsWith("2.11") &&
                     snap_211.contains("-SNAPSHOT") => snapshotScalaBinaryVersion
    case sv => sbt.CrossVersion.binaryScalaVersion(sv)
  }
}
