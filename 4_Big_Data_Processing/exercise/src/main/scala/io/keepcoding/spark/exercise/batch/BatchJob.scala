package io.keepcoding.spark.exercise.batch

import java.time.OffsetDateTime

import io.keepcoding.spark.exercise.streaming.StreamingJob.spark
import org.apache.spark.sql.catalyst.dsl.expressions.{DslExpression, StringToAttributeConversionHelper}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object BatchJob extends IBatchJob {

  def main(args: Array[String]): Unit = run(args)

  override val spark: SparkSession = SparkSession
    .builder()
    .master("local[20]")
    .appName("Ejercicio Final de Marcelo")
    .getOrCreate()

  override def readFromStorage(storagePath: String, filterDate: OffsetDateTime): DataFrame = {
    spark
      .read
      .format("parquet")
      .load(s"${storagePath}/data")
      .filter(
          $"hour" === filterDate.getHour
      )
  }

  override def readUserMetadata(jdbcURI: String, jdbcTable: String, user: String, password: String): DataFrame = {
    spark
      .read
      .format("jdbc")
      .option("url", jdbcURI)
      .option("dbtable", jdbcTable)
      .option("user", user)
      .option("password", password)
      .load()
  }

  import spark.implicits._

  override def enrichAntennaWithUserMetadata(antennaDF: DataFrame, user_metadataDF: DataFrame): DataFrame = {
    antennaDF.as("antenna")
      .join(
        user_metadataDF.as("user_metadata"),
        $"antenna.id" === $"user_metadata.id"
      ).drop($"user_metadata.id")
  }

  override def computeDevicesCountByAntena(dataFrame: DataFrame, app: String): DataFrame = {
    dataFrame
      .where($"app" === app)
      .select($"timestamp", $"value")
      .withWatermark("timestamp", "1 hour")
      .groupBy($"value", window($"timestamp", "1 hour"))
      .agg(
        sum($"bytes").as("value")
      ).select($"window.start".as("timestamp"), $"value")

  }

  override def writeToJdbc(dataFrame: DataFrame, jdbcURI: String, jdbcTable: String, user: String, password: String): Unit = {
    dataFrame
      .write
      .mode(SaveMode.Append)
      .format("jdbc")
      .option("driver", "org.postgresql.Driver")
      .option("url", jdbcURI)
      .option("dbtable", jdbcTable)
      .option("user", user)
      .option("password", password)
      .save()
  }

  override def writeToStorage(dataFrame: DataFrame, storageRootPath: String): Unit = {
    dataFrame
      .write
      .partitionBy("hour")
      .format("parquet")
      .mode(SaveMode.Overwrite)
      .save(s"${storageRootPath}/historical")
  }

}
