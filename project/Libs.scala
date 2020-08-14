import sbt._

object Versions {
  val zioVersion = "1.0.0"
}

object Libs {
  lazy val zio        = "dev.zio" %% "zio"          % Versions.zioVersion
  lazy val zioStreams = "dev.zio" %% "zio-streams"  % Versions.zioVersion
  lazy val zioTest    = "dev.zio" %% "zio-test"     % Versions.zioVersion
  lazy val zioTestSbt = "dev.zio" %% "zio-test-sbt" % Versions.zioVersion

}
