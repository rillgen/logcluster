organization := "com.despegar"

name := "logcluster"

scalaVersion := "2.9.2"

version := "0.1-SNAPSHOT"

publishTo := Some("nexus-snapshots" at "http://nexus.despegar.it:8080/nexus/content/repositories/snapshots/")

libraryDependencies ++= 
  "org.clapper" %% "grizzled-slf4j" % "0.6.9" ::
  "com.google.guava" % "guava" % "14.0.1" ::
  "joda-time" % "joda-time" % "2.2" ::
  // Required by guava
  "com.google.code.findbugs" % "jsr305" % "1.3.+" :: 
  Nil
 
scalacOptions += "-deprecation"
