val scala3Version = "3.0.0"
val AkkaVersion = "2.6.14"
lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3-simple",
    version := "0.1.0",

    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" % "akka-stream_2.13"  % AkkaVersion,
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "com.lightbend.akka" % "akka-stream-alpakka-mqtt_2.13" % "3.0.1",
      "org.json4s" % "json4s-native_2.13" % "4.0.0",
      "com.typesafe.akka" % "akka-stream_2.13" % "2.1.0"
    )
  )

