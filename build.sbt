name := "reactive-java8-akka"

version := "1.0"

scalaVersion := "2.10.3"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.0"
)
