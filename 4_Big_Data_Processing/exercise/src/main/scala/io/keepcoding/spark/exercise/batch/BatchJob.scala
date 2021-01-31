package io.keepcoding.spark.exercise.batch

import java.time.OffsetDateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.types.TimestampType

object BatchJob {

  //**************************************
  //**************************************
  def main(args: Array[String]): Unit = 
  {
    val Jdbc_url        = args(0)
    val Storage_url     = args(1)
    val filterDate      = OffsetDateTime.parse(args(2))
    val user_postgre    = "postgres"
    val pass_postgre    = "keepcoding"
    val driver_postgre  = "org.postgresql.Driver"

    val spark = SparkSession.builder()
      .master("local[20]")
      .appName("Ejercicio Final de Marcelo, segundo Intento")
      .getOrCreate()

    import spark.implicits._

    val user_metadata = spark
      .read
      .format("jdbc")
      .option("driver", driver_postgre)
      .option("url", Jdbc_url)
      .option("dbtable", "user_metadata")
      .option("user", user_postgre)
      .option("password", pass_postgre)
      .load()

    val device_data = spark
      .read
      .format("parquet")
      .option("path", s"${Storage_url}/data")
      .load()
      .filter(
        $"year"   === filterDate.getYear &&
        $"month"  === filterDate.getMonthValue &&
        $"day"    === filterDate.getDayOfMonth &&
        $"hour"   === filterDate.getHour
      )
      .persist()

    val totalCountBytesAntena = 
      computaBytesDeAntena(device_data, "id", "user_bytes_total", filterDate)
      .persist()

    val totalQuotaLimit = 
      totalCountBytesAntena.as("user")
      .select($"id", $"value")
      .join(
        user_metadata.select($"id", $"email", $"quota").as("metadata"),
        $"user.id" === $"metadata.id" && $"user.value" > $"metadata.quota"
      )
      .select($"metadata.email", $"user.value".as("usage"), $"metadata.quota", lit(filterDate.toEpochSecond).cast(TimestampType).as("timestamp"))

    Await.result(
      Future.sequence(
        Seq(
          guardadoATablas(
              computaBytesDeAntena(device_data, "antenna_id", "antenna_bytes_total", filterDate), 
              Jdbc_url, "bytes_hourly", user_postgre, pass_postgre, driver_postgre
          ),
          guardadoATablas(
              computaBytesDeAntena(device_data, "app", "app_bytes_total", filterDate), 
              Jdbc_url, "bytes_hourly", user_postgre, pass_postgre, driver_postgre
          ),
          guardadoATablas(totalCountBytesAntena, Jdbc_url, "bytes_hourly", user_postgre, pass_postgre, driver_postgre),
          guardadoATablas(totalQuotaLimit, Jdbc_url, "user_quota_limit", user_postgre, pass_postgre, driver_postgre)
        )
      ), 
      Duration.Inf
    )

  }
  //**************************************
  //**************************************
  def computaBytesDeAntena(
      dataFrame: DataFrame, 
      column: String, 
      metric: String, 
      filterDate: OffsetDateTime): DataFrame = 
  {
    import dataFrame.sparkSession.implicits._

    dataFrame
      .select(col(column).as("id"), $"bytes")
      .groupBy($"id")
      .agg(sum($"bytes").as("value"))
      .withColumn("type", lit(metric))
      .withColumn("timestamp", lit(filterDate.toEpochSecond).cast(TimestampType))
  }
  //**************************************
  //**************************************
  def guardadoATablas(
      dataFrame: DataFrame, 
      jdbcURI: String, 
      jdbcTable: String, 
      user: String, 
      password: String, 
      driver: String): Future[Unit] = Future 
  {
    dataFrame
      .write
      .mode(SaveMode.Append)
      .format("jdbc")
      .option("driver", driver)
      .option("url", jdbcURI)
      .option("dbtable", jdbcTable)
      .option("user", user)
      .option("password", password)
      .save()
  }

}
