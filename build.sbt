import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

name := """kafka-api"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(JavaAppPackaging)

scalaVersion := "2.12.8"

resolvers ++= Seq(
  "Confluent" at "https://packages.confluent.io/maven/",
  Resolver.mavenLocal,
  Resolver.jcenterRepo
)

coverageMinimum := 0
coverageFailOnMinimum := false

libraryDependencies += jdbc
libraryDependencies += ehcache
libraryDependencies += ws
libraryDependencies += guice
libraryDependencies += evolutions
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.3"
libraryDependencies += "org.mockito" % "mockito-core" % "2.9.0"
libraryDependencies += "com.opentable.components" % "otj-pg-embedded" % "0.8.0"
libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1208"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.+" % Test
libraryDependencies += "org.apache.kafka" %% "kafka" % "2.0.0"
libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "4.1.2"
libraryDependencies += "net.manub" %% "scalatest-embedded-kafka" % "1.0.0" % "test"
libraryDependencies += "io.swagger" %% "swagger-play2" % "1.6.+"
coverageExcludedPackages := "<empty>;Reverse.*;views.*;router.*;database.*"

javaOptions in Test ++= Seq("-Dconfig.file=conf/application.test.conf", "-Dplay.evolutions.db.default.autoApply=true")

val preferences =
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AlignParameters, true)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(DanglingCloseParenthesis, Preserve)

SbtScalariform.scalariformSettings ++ Seq(preferences)
