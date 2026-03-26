lazy val root = (project in file("."))
  .settings(
    name := "StreamProcessor",
    version := "1.0",
    scalaVersion := "2.12.15"
  )

val sparkVersion = "3.5.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",

  "org.apache.spark" %% "spark-avro" % sparkVersion ,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion ,

  "com.datastax.spark" %% "spark-cassandra-connector" % "3.5.0",

  "com.typesafe" % "config" % "1.4.1"
)

javaOptions += "-Dconfig.resource=deployment.conf"

assembly / assemblyJarName := "streamprocessor-assembly-1.0.jar"

assembly / assemblyMergeStrategy := {
  case "reference.conf" => MergeStrategy.concat
  case "META-INF/services/org.apache.spark.sql.sources.DataSourceRegister" => MergeStrategy.concat
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}