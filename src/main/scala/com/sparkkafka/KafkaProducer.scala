package com.sparkkafka

import org.apache.spark.SparkContext
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Dataset, Row, SparkSession}

object KafkaProducer extends App {

  implicit lazy val spark: SparkSession = SparkSession
    .builder()
    .appName("kafkaProducer")
    .master("local[*]")
    .getOrCreate()
  implicit lazy val sc: SparkContext = spark.sparkContext

  import spark.implicits._
  val sampleData =
    """
      |programname,id,waterhardness
      |Eco,id1,H02
      |Auto,id1,H04
      |NightWash,id2,H02
      |NightWash,id2,H01
      |Auto,id1,H02
      |Auto,id1,H00
      |Auto,id1,H05
      |""".stripMargin.lines.toList
  val csvData: Dataset[String] = sc.parallelize(sampleData).toDS()
  val csvRDD = spark.read
    .option("header", "true")
    .option("inferSchema", "true")
    .csv(csvData)
    .rdd
  val transform = csvRDD
    .map { row =>
      val m = row.getValuesMap(row.schema.fieldNames)
      (m.getOrElse("id", "NotSet"), m)
    }
    .groupByKey()
    .map(
      data =>
        (
          data._1,
          data._2
            .flatMap(_.map(mappedData => s"${mappedData._1}: ${mappedData._2}"))
            .toSeq
            .mkString(",")
        )
    )

  val kafkaSchema = new StructType()
    .add(StructField("key", StringType, true))
    .add(StructField("value", StringType, true))

  val kafkaDF = spark.createDataFrame(
    transform.zipWithIndex.map {
      case (data, index) =>
        Row(index.toString, s"${data._1} => ${data._2}")
    },
    kafkaSchema
  )

  kafkaDF
    .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    .write
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("topic", "test-topic")
    .save()

}
