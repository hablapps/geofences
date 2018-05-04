name := "funinpractice_codemotion17"

scalaBinaryVersion := "2.12"

scalaVersion := "2.12.3"

organization := "org.hablapps"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")

addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)

lazy val doobieVersion = "0.4.2"
lazy val shapelessVersion = "2.3.2"

parallelExecution in Test := false

scalacOptions ++= Seq(
  "-Ypartial-unification")

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.8",
  "org.tpolecat" %% "doobie-core"       % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"   % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest"  % doobieVersion,
  "org.hablapps" %% "puretest-scalaz" % "0.3.2",
  "org.http4s" %% "http4s-dsl" % "0.18.0",
  "org.http4s" %% "http4s-blaze-server" % "0.18.0",
  "org.http4s" %% "http4s-blaze-client" % "0.18.0",
  "org.http4s" %% "http4s-circe" % "0.18.0",
  "io.circe" %% "circe-generic" % "0.9.0-M2",
  "io.circe" %% "circe-literal" % "0.9.0-M2",
  "ch.qos.logback" %  "logback-classic" % "1.2.3" % "runtime")

