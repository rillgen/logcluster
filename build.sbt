organization := "com.despegar"

name := "logcluster"

scalaVersion := "2.10.2"

version := "0.2-SNAPSHOT"

publishTo := Some("snapshots" at "http://nexus.despegar.it:8080/nexus/content/repositories/snapshots/")
//publishTo := Some("snapshots" at "http://nexus:8080/nexus/content/repositories/snapshots-miami")

libraryDependencies ++= 
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1" ::
  "com.google.guava" % "guava" % "15.0" ::
  "joda-time" % "joda-time" % "2.3" ::
  // Required by guava
  "com.google.code.findbugs" % "jsr305" % "1.3.+" :: 
  Nil
 
scalacOptions ++= Seq("-deprecation", "-feature", "-language:reflectiveCalls")
