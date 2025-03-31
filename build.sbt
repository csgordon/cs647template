val scala3Version = "3.3.5"
lazy val akkaVersion = "2.10.0"
lazy val root = project
  .in(file("."))
  .settings(
    name := "akkademo1",
    version := "0.1.0-SNAPSHOT",
    fork := true,
    connectInput in run := true, // This is required for console input to work!

    // This is required as a workaround for https://github.com/lampepfl/dotty/issues/14846
    compileOrder := CompileOrder.JavaThenScala,
    scalaVersion := scala3Version,
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.5.8",
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
)
  )
