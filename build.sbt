import Libs._
ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.reaktive-carrot"
ThisBuild / organizationName := "reaktive-carrot"
// ThisBuild / semanticdbEnabled := true
// ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val scalaCompilerOptions = Seq(
  "-encoding",
  "UTF-8",
  "-Ywarn-dead-code",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-inaccessible",
  "-language:higherKinds",
  "-language:existentials"
)

lazy val commonDependencies = Seq(
  Libs.zio,
  Libs.zioStreams,
  Libs.zioTest    % Test,
  Libs.zioTestSbt % Test
)
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

lazy val root = (project in file("."))
  .settings(
    name := "basket-score",
    libraryDependencies ++= commonDependencies
  )

// CMD ALIASES
// format all sources and tests with scala fmt
addCommandAlias("format", ";scalafmt;test:scalafmt")
