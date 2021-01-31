package io.keepcoding.spark.exercise.streaming

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StringType, StructType, LongType, StructField, TimestampType}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object StreamingJob {

  //**************************************
  //**************************************
  def main(args: Array[String]): Unit = 
  {
    val Server          = args(0)
    val Jdbc_url        = args(1)
    val Storage_url     = args(2)
    val user_postgre    = "postgres"
    val pass_postgre    = "keepcoding"
    val driver_postgre  = "org.postgresql.Driver"

    val spark = SparkSession.builder()
      .master("local[20]")
      .appName("Ejercicio Final de Marcelo")
      .getOrCreate()

    import spark.implicits._

    val schema = StructType(
      Seq(
        StructField("timestamp", LongType, nullable = false),
        StructField("id", StringType, nullable = false),
        StructField("antenna_id", StringType, nullable = false),
        StructField("bytes", LongType, nullable = false),
        StructField("app", StringType, nullable = false)
      )
    )

    val device = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", Server)
      .option("subscribe", "devices")
      .option("startingOffsets", "earliest")
      .load()
      .select(from_json($"value".cast(StringType), schema).as("json"))
      .select($"json.*")

    val storage = Future {
      device
        .select(
          $"id", $"antenna_id", $"bytes", $"app",
          year($"timestamp".cast(TimestampType)).as("year"),
          month($"timestamp".cast(TimestampType)).as("month"),
          dayofmonth($"timestamp".cast(TimestampType)).as("day"),
          hour($"timestamp".cast(TimestampType)).as("hour")
        )
        .writeStream
        .partitionBy("year", "month", "day", "hour")
        .format("parquet")
        .option("path", s"${Storage_url}/data")
        .option("checkpointLocation", s"${Storage_url}/checkpoint")
        .start()
        .awaitTermination()
    }

    Await.result(
      Future.sequence(
        Seq(
          bytesTotales(device, "app", Jdbc_url, user_postgre, pass_postgre, driver_postgre), 
          bytesTotales(device.withColumnRenamed("id", "user"), "user", Jdbc_url, user_postgre, pass_postgre, driver_postgre), 
          bytesTotales(device.withColumnRenamed("antenna_id", "antenna"), "antenna", Jdbc_url, user_postgre, pass_postgre, driver_postgre), 
          storage
        )
      ), 
      Duration.Inf
    )

  }
  //**************************************
  //**************************************
  def bytesTotales(
    dataFrame: DataFrame, 
    column: String, 
    jdbcURI: String,
    user: String, 
    password: String, 
    driver: String): Future[Unit] = Future 
  {
    import dataFrame.sparkSession.implicits._

    dataFrame
      .select($"timestamp".cast(TimestampType).as("timestamp"), col(column), $"bytes")
      .withWatermark("timestamp", "6 minutes")
      .groupBy(window($"timestamp", "5 minutes"), col(column))
      .agg(sum($"bytes").as("total_bytes"))
      .select(
        $"window.start".cast(TimestampType).as("timestamp"),
        col(column).as("id"),
        $"total_bytes".as("value"),
        lit(s"${column}_total_bytes").as("type")
      )
      .writeStream
      .foreachBatch((dataset: DataFrame, batchId: Long) =>
        dataset
          .write
          .mode(SaveMode.Append)
          .format("jdbc")
          .option("driver", driver)
          .option("url", jdbcURI)
          .option("dbtable", "bytes")
          .option("user", user)
          .option("password", password)
          .save()
      )
      .start()
      .awaitTermination()
  }
}