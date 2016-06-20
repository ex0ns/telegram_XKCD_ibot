name := "InlineXKCD"
version := "0.1"
scalaVersion := "2.11.7"

resolvers += "jitpack" at "https://jitpack.io"
scalacOptions ++= Seq("-feature")

libraryDependencies ++= Seq(
  "com.github.mukel" %% "telegrambot4s" % "v1.2.0",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1",
  "fr.hmil" %% "scala-http-client" % "0.3.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
)

unmanagedResourceDirectories in Compile += { baseDirectory.value / "keys/telegram.key" }