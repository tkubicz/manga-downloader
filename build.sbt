name := "manga-downloader"
organization := "eu.lynxware"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.11.8"

lazy val slf4jVersion = "1.7.21"
lazy val logbackVersion = "1.1.7"
lazy val scalaLoggingVersion = "3.4.0"
lazy val scalatestVersion = "3.0.0"
lazy val scalaXmlVersion = "1.0.6"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion,

  "org.scalatest" %% "scalatest" % scalatestVersion % "test"
)