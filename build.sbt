name := "TubeLytics"
organization := "ca.concordia"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"

jacocoReportSettings := JacocoReportSettings().withFormats(JacocoReportFormats.XML, JacocoReportFormats.HTML)

libraryDependencies += guice
libraryDependencies += "org.asynchttpclient" % "async-http-client" % "2.12.3"
libraryDependencies += "org.json" % "json" % "20210307"
libraryDependencies += "org.mockito" % "mockito-core" % "2.10.0" % "test"
