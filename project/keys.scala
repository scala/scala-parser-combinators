object TestKeys {
  import sbt.settingKey

  // for testing with partest
  val includeTestDependencies = settingKey[Boolean]("Doesn't declare test dependencies.")
}