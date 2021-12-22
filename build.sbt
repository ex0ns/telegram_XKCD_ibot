import scala.io.Source

name := "InlineXKCD"
organization := "ex0ns"
version := "0.1"
scalaVersion := "2.13.6"

scalacOptions ++= Seq("-feature")

libraryDependencies ++= Seq(
  "com.bot4s" %% "telegram-core" % "5.2.0",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.8.0",
  "com.softwaremill.sttp.client3" %% "core" % "3.3.17",
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-future" % "3.3.17",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3",
  "eu.timepit" %% "fs2-cron-cron4s" % "0.7.1",
)

enablePlugins(DockerPlugin)

docker / dockerfile := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("java")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}

envVars := Map("TELEGRAM_KEY" -> Source.fromFile("telegram.key").getLines().next)

assembly / assemblyMergeStrategy := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}
