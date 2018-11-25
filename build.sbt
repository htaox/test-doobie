name := "test-doobie"
organization in ThisBuild := "com.eztier"
scalaVersion in ThisBuild := "2.12.4"

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8",
  "-Ylog-classpath",
  "-Ypartial-unification"
)

lazy val global = project
  .in(file("."))
  .settings(settings)
  .aggregate(
    common,
    database,
    http
  )
  
lazy val commonSettings = Seq(
  version := "0.1.1",
  organization := "com.eztier",
  scalaVersion := "2.12.4",
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("public"),
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val settings = commonSettings

lazy val common = project
  .settings(
    name := "common",
    settings,
    libraryDependencies ++= Seq(
      scalaTest,
      logback
    )
  )

val akka = "com.typesafe.akka"
val akkaHttpV = "10.1.5"

val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

val akkaStream = akka %% "akka-stream" % "2.5.18"
val akkaSlf4j = akka %% "akka-slf4j" % "2.5.18"
val akkaStreamTestkit = akka %% "akka-stream-testkit" % "2.5.18" % Test
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

// HTTP server
val akkaHttp = akka %% "akka-http" % akkaHttpV
val akkaHttpCore = akka %% "akka-http-core" % akkaHttpV
val akkaHttpSprayJson = akka %% "akka-http-spray-json" % akkaHttpV
val akkaHttpTestkit = akka %% "akka-http-testkit" % akkaHttpV % Test

// Support of CORS requests, version depends on akka-http
// val akkaHttpCors = "ch.megard" %% "akka-http-cors" % "0.3.0"

// PostgreSQL
val doobie = "org.tpolecat" %% "doobie-core"      % "0.6.0"
val doobieH2 = "org.tpolecat" %% "doobie-h2"        % "0.6.0"          // H2 driver 1.4.197 + type mappings.
val doobieHikari = "org.tpolecat" %% "doobie-hikari"    % "0.6.0"          // HikariCP transactor.
val doobiePostgres = "org.tpolecat" %% "doobie-postgres"  % "0.6.0"          // Postgres driver 42.2.5 + type mappings.
val doobiePostgresCirce = "org.tpolecat" %% "doobie-postgres-circe"  % "0.6.0"          // Postgres driver 42.2.5 + type mappings.
val doobieSpecs2 = "org.tpolecat" %% "doobie-specs2"    % "0.6.0" % "test" // Specs2 support for typechecking statements.
val doobieScalaTest = "org.tpolecat" %% "doobie-scalatest" % "0.6.0" % "test"  // ScalaTest support for typechecking statements.

// circe
val circeGenericExtras = "io.circe" %% "circe-generic-extras" % "0.10.0"

lazy val database = project.
  settings(
    name := "database",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      scalaTest,
      logback,
      akkaStream,
      akkaSlf4j,
      akkaStreamTestkit,
      doobiePostgres,
      doobiePostgresCirce,
      doobieScalaTest,
      circeGenericExtras         
    )
  ).dependsOn(
    common
  )

lazy val http = project.
  settings(
    name := "http",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      scalaTest,
      logback,
      akkaStream,
      akkaSlf4j,
      akkaStreamTestkit,
      akkaHttp,
      akkaHttpCore,
      akkaHttpSprayJson,
      akkaHttpTestkit
    )
  ).dependsOn(
    common,
    database
  )
  
lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _                             => MergeStrategy.first
  }
)
