package io.keepcoding.spark.exercise.streaming

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.functions.{col, from_json, hour, sum, window}
import org.apache.spark.sql.types.{StringType, StructType}

import scala.concurrent.Future
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import scala.concurrent.ExecutionContext.Implicits.global

object StreamingJob extends IStreamingJob {

  //  Método MAIN inicializador del Objeto
  //----------------------------------------
  def main(args: Array[String]): Unit = run(args)

  //  Para leer de la API de Spark
  //----------------------------------------
  override val spark: SparkSession = SparkSession
    .builder()
    .master("local[20]")
    .appName("Ejercicio Final de Marcelo")
    .getOrCreate()

  //  Lectura de los datos que nos llegan desde Kafka, devolviendo un DataFrame
  //----------------------------------------
  override def readFromKafka(kafkaServer: String, topic: String): DataFrame = {
    spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaServer)
      .option("subscribe", topic)
      .load()
  }

  //  Parseamos el json Antenna
  //----------------------------------------
  override def parserJsonData(dataFrame: DataFrame): DataFrame = {
    val schema = ScalaReflection.schemaFor[AntennaMessage].dataType.asInstanceOf[StructType]

    dataFrame
      .select(from_json(col("value").cast(StringType), schema).as("json"))
      .select("json.*")
  }

  //  Leemos los datos del dataset estático de user_metadatos en postgreSQL
  //----------------------------------------
  override def readUserMetadata(jdbcURI: String, jdbcTable: String, user: String, password: String): DataFrame = {
    spark
      .read
      .format("jdbc")
      .option("driver", "org.postgresql.Driver")
      .option("url", jdbcURI)
      .option("dbtable", jdbcTable)
      .option("user", user)
      .option("password", password)
      .load()
  }

  import spark.implicits._

  // Hacemos un join de los datos de las antenas en tiempo real con los user_metadata
  //----------------------------------------
  override def enrichAntennaWithUserMetadata(antennaDF: DataFrame, user_metadataDF: DataFrame): DataFrame = {
    antennaDF.as("antenna")
      .join(
        user_metadataDF.as("user_metadata"),
        $"antenna.id" === $"user_metadata.id"
      ).drop($"user_metadata.id")
  }

  //  Ahora salvamos en el Storage los datos que estarán filtrados por Horas
  //----------------------------------------
  override def writeToStorage(dataFrame: DataFrame, storageRootPath: String): Future[Unit] = Future{
    dataFrame
      .withColumn("hour", hour($"timestamp"))
      .writeStream
      .partitionBy("hour")
      .format("parquet")
      .option("path", s"${storageRootPath}/data")
      .option("checkpointLocation", s"${storageRootPath}/checkpoint")
      .start()
      .awaitTermination()
  }

  //  Sumatorio de todos los datos de la tabla enviada por parametro app
  //----------------------------------------
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

  //  Salvado de los datos en la tabla creada
  //----------------------------------------
  override def writeToJdbc(dataFrame: DataFrame, jdbcURI: String, jdbcTable: String, user: String, password: String): Future[Unit] = Future {
    dataFrame
      .writeStream
      .foreachBatch { (data: DataFrame, batchId: Long) =>
        data
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
      .start()
      .awaitTermination()
  }
}
