val scala3Version = "3.6.4"

lazy val root = project
  .in(file("."))
  .settings(
    maintainer := "chunkwong.wong@gmail.com",
    name := "agg3",
    version := "0.1.0",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "com.opencsv" % "opencsv" % "5.10"
    ),

    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )

enablePlugins(JavaAppPackaging)
