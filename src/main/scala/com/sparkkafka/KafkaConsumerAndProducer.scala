package com.sparkkafka

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, TaskContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object KafkaConsumerAndProducer extends App {
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  val appName = "KafkaConsumerAndProducer"
  val master = "local[2]"
  val conf = new SparkConf().setAppName(appName).setMaster(master)
  val ssc = new StreamingContext(conf, Seconds(2))

  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "127.0.0.1:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "use_a_separate_group_id_for_each_stream2",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  val topics = Array("test-topic")
  val stream = KafkaUtils.createDirectStream[String, String](
    ssc,
    PreferConsistent,
    Subscribe[String, String](topics, kafkaParams)
  )

  stream.foreachRDD { rdd =>
    rdd.foreach(cr => println(s"# consumerrecord: ${cr.key} -> ${cr.value()}"))

    val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
    rdd.foreachPartition { iter =>
      val o: OffsetRange = offsetRanges(TaskContext.get.partitionId)
      println(
        s"########### ${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}"
      )
    }
  }

  val convertIt = stream.map { record =>
    println(s"# convertIt recordvalue: ${record.value}")
    (record.key, record.value)
  }

  convertIt.foreachRDD { rdd =>
    rdd.foreach(
      cr => println(s"# convertIt: consumerrecord: ${cr._1} -> ${cr._2}")
    )
  }

  ssc.start()
  ssc.awaitTermination() // Wait for the computation to terminate

}
