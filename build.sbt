organization := "com.despegar"

name := "logcluster"

scalaVersion := "2.9.2"

version := "0.1-SNAPSHOT"

resolvers += "sonatype-public" at "https://oss.sonatype.org/content/groups/public"

publishTo := Some("Despegar Nexus" at "http://vmtilcara.servers.despegar.it:8080/nexus/content/repositories/snapshots/")

libraryDependencies ++= 
  "org.clapper" %% "grizzled-slf4j" % "0.6.9" ::
  "com.google.guava" % "guava" % "12.0.1" ::
  "joda-time" % "joda-time" % "2.1" ::
  Nil
 
scalacOptions += "-deprecation"
