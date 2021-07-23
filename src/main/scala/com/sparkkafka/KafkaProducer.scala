package com.sparkkafka

import org.apache.spark.SparkContext
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Dataset, Row, SparkSession}

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
case class KafkaJSON(id: String, lable: String, value: String)

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
      |id,lable,value
      |id1,lable1,value1
      |id1,lable2,value1
      |id1,lable1,value2
      |id1,lable1,value1
      |id1,lable2,value1
      |id2,lable1,value2
      |id2,lable1,value1
      |id2,lable2,value1
      |id2,lable1,value2
      |id2,lable1,value2
      |id2,lable1,value2
      |id2,lable1,value2
      |id2,lable1,value2
      |""".stripMargin.lines.toList

  val csvData: Dataset[String] = sc.parallelize(sampleData).toDS()
  val csvRDD = spark.read
    .option("header", "true")
    .option("inferSchema", "true")
    .csv(csvData)
    .rdd

  val kafkaJSONRDD = csvRDD
    .map { row =>
      val m = row.getValuesMap(row.schema.fieldNames)
      KafkaJSON(
        m.getOrElse("id", "NotSet"),
        m.getOrElse("lable", "NotSet"),
        m.getOrElse("value", "NotSet")
      )
    }

  val kafkaSchema = new StructType()
    .add(StructField("key", StringType, true))
    .add(StructField("value", StringType, true))

  val kafkaDF = spark.createDataFrame(
    kafkaJSONRDD.zipWithIndex.map {
      case (data, index) =>
        Row(index.toString, data.asJson.noSpaces)
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
