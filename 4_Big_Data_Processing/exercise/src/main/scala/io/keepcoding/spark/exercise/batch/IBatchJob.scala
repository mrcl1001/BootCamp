package io.keepcoding.spark.exercise.batch

import java.time.OffsetDateTime

import org.apache.spark.sql.{DataFrame, SparkSession}

case class AntennaMessage(hour: Int, id: String, metric: String, value: Long)

trait IBatchJob {

  val spark: SparkSession

  def readFromStorage(storagePath: String, filterDate: OffsetDateTime): DataFrame

  def readUserMetadata(jdbcURI: String, jdbcTable: String, user: String, password: String): DataFrame

  def enrichAntennaWithUserMetadata(antennaDF: DataFrame, user_metadataDF: DataFrame): DataFrame

  def computeDevicesCountByAntena(dataFrame: DataFrame, app: String): DataFrame

  def writeToJdbc(dataFrame: DataFrame, jdbcURI: String, jdbcTable: String, user: String, password: String): Unit

  def writeToStorage(dataFrame: DataFrame, storageRootPath: String): Unit

  def run(args: Array[String]): Unit = {
    val Array(filterDate, storagePath, jdbcUri, jdbcMetadataTable, aggJdbcTable, aggJdbcErrorTable, aggJdbcPercentTable, jdbcUser, jdbcPassword) = args
    println(s"Running with: ${args.toSeq}")

    val antennaDF = readFromStorage(storagePath, OffsetDateTime.parse(filterDate))
    val metadataDF = readUserMetadata(jdbcUri, jdbcMetadataTable, jdbcUser, jdbcPassword)
    val antennaUserMetadataDF = enrichAntennaWithUserMetadata(antennaDF, metadataDF).cache()
    val aggByBytesDF = computeDevicesCountByAntena(antennaUserMetadataDF, "FACEBOOK")

    writeToJdbc(aggByBytesDF, jdbcUri, aggJdbcTable, jdbcUser, jdbcPassword)

    writeToStorage(antennaDF, storagePath)

    spark.close()
  }

}
