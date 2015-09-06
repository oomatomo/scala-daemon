name := "scala-deamon"

organization := "com.oomatomo"

version := "0.0.1"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.13",
  "org.specs2" %% "specs2" % "2.4" % "test"
)

enablePlugins(JavaAppPackaging)

scalacOptions in Test ++= Seq("-Yrangepos")

// Read here for optional dependencies:
// http://etorreborre.github.io/specs2/guide/org.specs2.guide.Runners.html#Dependencies

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

initialCommands := "import com.oomatomo.scaladeamon._"
