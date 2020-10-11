package io.keepcoding.spark.exercise.streaming

import java.sql.Timestamp

import io.keepcoding.spark.exercise.streaming.StreamingJob.{computeDevicesCountByAntena, enrichAntennaWithUserMetadata}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import org.apache.spark.sql.{DataFrame, SparkSession}

/*
  El json que lanza kafka es de este formato, por lo que la clase tendrá que tener estos valores
 {"id":"11111111-1111-1111-1111-111111111111","metric":"devices_count","value":12}
*/
case class AntennaMessage(hour: Int, id: String, metric: String, value: Long)

trait IStreamingJob {

  val spark: SparkSession

  def readFromKafka(kafkaServer: String, topic: String): DataFrame

  def parserJsonData(dataFrame: DataFrame): DataFrame

  def readUserMetadata(jdbcURI: String, jdbcTable: String, user: String, password: String): DataFrame

  def enrichAntennaWithUserMetadata(antennaDF: DataFrame, user_metadataDF: DataFrame): DataFrame

  def computeDevicesCountByAntena(dataFrame: DataFrame, app: String): DataFrame

  def writeToJdbc(dataFrame: DataFrame, jdbcURI: String, jdbcTable: String, user: String, password: String): Future[Unit]

  def writeToStorage(dataFrame: DataFrame, storageRootPath: String): Future[Unit]

  def run(args: Array[String]): Unit = {
    val Array(kafkaServer, topic, jdbcUri, jdbcMetadataTable, bytes_hourlyJdbcTable, jdbcUser, jdbcPassword, storagePath) = args
    println(s"Running with: ${args.toSeq}")

    val kafkaDF = readFromKafka(kafkaServer, topic)
    val antennaDF = parserJsonData(kafkaDF)

    /*
    antennaDF
      .writeStream
      .format("console")
      .start()
      .awaitTermination()
    */

    val user_metadataDF = readUserMetadata(jdbcUri, jdbcMetadataTable, jdbcUser, jdbcPassword)
    val antennaMetadataDF = enrichAntennaWithUserMetadata(antennaDF, user_metadataDF)

    /*
   antennaMetadataDF
     .writeStream
     .format("console")
     .start()
     .awaitTermination()
   */

    val storageFuture = writeToStorage(antennaDF, storagePath)

    var aggByBytesDF = computeDevicesCountByAntena(antennaMetadataDF, "FACEBOOK")
    var aggFuture = writeToJdbc(aggByBytesDF, jdbcUri, bytes_hourlyJdbcTable, jdbcUser, jdbcPassword)
    aggByBytesDF = computeDevicesCountByAntena(antennaMetadataDF, "TELEGRAM")
    aggFuture = writeToJdbc(aggByBytesDF, jdbcUri, bytes_hourlyJdbcTable, jdbcUser, jdbcPassword)
    aggByBytesDF = computeDevicesCountByAntena(antennaMetadataDF, "FACETIME")
    aggFuture = writeToJdbc(aggByBytesDF, jdbcUri, bytes_hourlyJdbcTable, jdbcUser, jdbcPassword)
    aggByBytesDF = computeDevicesCountByAntena(antennaMetadataDF, "SKYPE")
    aggFuture = writeToJdbc(aggByBytesDF, jdbcUri, bytes_hourlyJdbcTable, jdbcUser, jdbcPassword)

    Await.result(Future.sequence(Seq(aggFuture, storageFuture)), Duration.Inf)

    spark.close()
  }
}
