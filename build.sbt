name := "TubeLytics"
organization := "ca.concordia"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"

libraryDependencies += guice
libraryDependencies += "org.asynchttpclient" % "async-http-client" % "2.12.3"
libraryDependencies += "org.json" % "json" % "20210307"
libraryDependencies += "org.junit.jupiter" % "junit-jupiter-api" % "5.7.0" % Test
libraryDependencies += "org.junit.jupiter" % "junit-jupiter-engine" % "5.7.0" % Test
libraryDependencies ++= Seq(
  "org.mockito" % "mockito-core" % "3.9.0" % Test,  // For mocking dependencies
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,  // Play test framework
  "com.typesafe.play" %% "play-test" % "2.8.18" % Test  // Play's built-in test support
)

