lazy val commonSettings = Seq(
  organization := "ru.ifmo",
  libraryDependencies += junitInterface,
  scalacOptions ++= Seq("-deprecation", "-feature", "-opt-warnings:_", "-unchecked"),
  scalaVersion := "2.13.18",
  fork := true
)

lazy val junitInterface = "com.github.sbt" % "junit-interface" % "0.13.3" % "test"

lazy val root = project
  .in(file("."))
  .settings(commonSettings*)
  .settings(name    := "incremental-orthants",
            version := "0.0.0")
  .dependsOn(implementations, benchmarking)
  .aggregate(implementations, benchmarking)

lazy val implementations = project
  .in(file("implementations"))
  .settings(commonSettings*)
  .settings(name    := "incremental-orthants-implementations",
            version := "0.0.0")

lazy val benchmarking = project
  .in(file("benchmarking"))
  .settings(commonSettings*)
  .settings(name    := "incremental-orthants-benchmarking",
            version := "0.0.0")
  .dependsOn(implementations)
  .enablePlugins(JmhPlugin)
