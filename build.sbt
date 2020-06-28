import Libs._

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.reaktive-carrot"
ThisBuild / organizationName := "reaktive-carrot"

lazy val scalaCompilerOptions = Seq(
  "-encoding",
  "UTF-8",
  "-Ywarn-dead-code",
  "-Ywarn-unused-import",
  "-Ywarn-inaccessible",
  "-language:higherKinds",
  "-language:existentials"
)

lazy val commonDependencies = Seq(
  Libs.zio,
  Libs.zioTest    % Test,
  Libs.zioTestSbt % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "basket-score",
    libraryDependencies ++= commonDependencies
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
