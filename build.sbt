lazy val commonSettings = Seq(
    organization := "com.github.miiwo",
    scalaVersion := "2.13.4",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint", "-Ywarn-dead-code")  
)

lazy val noPublishSettings = Seq(
  publish := {}, 
  publishLocal := {}, 
  publishArtifact := false
)

lazy val herokuSettings = Seq(
  herokuAppName in Compile := "beach-bunker-discord-bot",
  herokuFatJar in Compile := Some((assemblyOutputPath in assembly).value)
)

lazy val ackcordSettings = Seq(
  resolvers += Resolver.JCenterRepository,
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic"         % "1.2.3",
    "net.katsstuff" %% "ackcord"                 % "0.17.1", //For high level API, includes all the other modules (core, commands, lavaplayer)
  ),
  dependencyOverrides += "com.sedmelluq" % "lavaplayer" % "1.3.63",
)

lazy val client = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    commonSettings, noPublishSettings, ackcordSettings, herokuSettings)
  .settings(
    name := "ackmiBot",
    version := "0.1",
    assemblyMergeStrategy in assembly := { // This was possibly needed because I was being baka about library dependencies
      case "module-info.class" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )