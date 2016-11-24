name := "manga-downloader"
organization := "eu.lynxware"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.12.0"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",

  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)