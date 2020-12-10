name := "ackmiBot"

version := "0.1"

scalaVersion := "2.13.4"

lazy val commonSettings = Seq(
    scalaVersion := "2.13.4",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint", "-Ywarn-dead-code")
)

lazy val noPublishSettings = Seq(publish := {}, publishLocal := {}, publishArtifact := false)

resolvers += Resolver.JCenterRepository

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "net.katsstuff" %% "ackcord"                 % "0.17.1" //For high level API, includes all the other modules
libraryDependencies += "net.katsstuff" %% "ackcord-core"            % "0.17.1" //Low level core API
libraryDependencies += "net.katsstuff" %% "ackcord-commands"        % "0.17.1" //Low to mid level Commands API
libraryDependencies += "net.katsstuff" %% "ackcord-lavaplayer-core" % "0.17.1" //Low level lavaplayer API

enablePlugins(JavaServerAppPackaging)