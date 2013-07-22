organization := "com.despegar"

name := "logcluster"

crossScalaVersions := Seq("2.9.2", "2.10.0")

version := "0.1-SNAPSHOT"

publishTo := Some("nexus-snapshots" at "http://nexus.despegar.it:8080/nexus/content/repositories/snapshots/")

libraryDependencies ++= 
  "org.clapper" %% "grizzled" % "1.0.1" ::
  "com.google.guava" % "guava" % "14.0.1" ::
  "joda-time" % "joda-time" % "2.2" ::
  // Required by guava
  "com.google.code.findbugs" % "jsr305" % "1.3.+" :: 
  Nil
 
scalacOptions += "-deprecation"
