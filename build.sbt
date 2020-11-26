name := "spark-kafka"
organization := "com.spark-kafka"
scalaVersion := "2.11.12"

//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.2" % Runtime

val sparkVersion = "2.4.5"
// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion

// https://mvnrepository.com/artifact/org.apache.spark/spark-sql-kafka-0-10
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion
