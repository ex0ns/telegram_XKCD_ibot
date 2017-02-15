import scala.io.Source

name := "InlineXKCD"
organization := "ex0ns"
version := "0.1"
scalaVersion := "2.11.8"

resolvers += Resolver.sonatypeRepo("snapshots")
scalacOptions ++= Seq("-feature")

libraryDependencies ++= Seq(
  "info.mukel" %% "telegrambot4s" % "2.1.0-SNAPSHOT",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1",
  "fr.hmil" %% "scala-http-client" % "0.3.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "com.github.philcali" %% "cronish" % "0.1.3"
)

enablePlugins(DockerPlugin, DockerComposePlugin)

dockerfile in docker := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("java")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}

variablesForSubstitution := Map("TELEGRAM_KEY" -> Source.fromFile("telegram.key").getLines().next)

dockerImageCreationTask := docker.value

