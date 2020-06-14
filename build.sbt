organization := "com.despegar"

name := "logcluster"

scalaVersion := "2.12.11"

version := "0.4-SNAPSHOT"

libraryDependencies ++=
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2" ::
  "com.github.blemale" %% "scaffeine" % "3.1.0" % "compile" ::
  Nil
 
scalacOptions ++= Seq("-deprecation", "-feature", "-language:reflectiveCalls")
