import sbt._

object Versions {
  val zioVersion = "1.0.0-RC20"
}

object Libs {
  lazy val zio          = "dev.zio" %% "zio"            % Versions.zioVersion
  lazy val zioTest      = "dev.zio" %% "zio-test"       % Versions.zioVersion
  lazy val zioTestSbt   = "dev.zio" %% "zio-test-sbt"   % Versions.zioVersion
//  lazy val zioTestJunit = "dev.zio" %% "zio-test-junit" % Versions.zioVersion

}
