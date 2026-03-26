import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.avro.functions._
import org.apache.spark.sql.streaming._
import org.apache.spark.sql.types._
import org.apache.spark.sql.cassandra._
import scala.collection.JavaConverters._
import com.datastax.oss.driver.api.core.uuid.Uuids
import com.datastax.spark.connector._

import com.typesafe.config.{Config, ConfigFactory}
import scala.io.Source

object StreamProcessor {
  def main(args: Array[String]): Unit = 
  {
    // load config   
    val conf: Config = ConfigFactory.load()
    val settings: Settings = new Settings(conf)

    // load trades chema
    val tradesSchema: String = Source.fromInputStream(
      getClass.getResourceAsStream(settings.schemas("trades"))
    ).mkString

    //udf for Cassandra uuids
    val makeUUID = udf(() => Uuids.timeBased().toString  )


    val spark = SparkSession
      .builder
      .master(settings.spark("master"))
      .appName(settings.spark("appName"))
      .config("spark.cassandra.connection.host",settings.cassandra("host"))
      .config("spark.cassandra.connection.host",settings.cassandra("host"))
      .config("spark.cassandra.auth.username", settings.cassandra("username"))
      .config("spark.cassandra.auth.password", settings.cassandra("password"))
      .config("spark.sql.shuffle.partitions", settings.spark("shuffle_partitions"))
      .getOrCreate()

    import spark.implicits._

                  // read kafka 
    val inputDF = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers","kafka:9092")
      .option("subscribe",settings.kafka("topic_market"))
      .option("minPartitions", settings.kafka("min_partitions"))
      .option("maxOffsetsPerTrigger", settings.spark("max_offsets_per_trigger"))
      .option("useDeprecatedOffsetFetching",settings.spark("deprecated_offsets"))
      .load()

                 // parse avro 
    val expandedDF = inputDF
            .withColumn("avroData",from_avro(col("value"),tradesSchema,Map("mode" -> "PERMISSIVE").asJava))
            .select($"avroData.*")
            .select(explode($"data"),$"type")
            .select($"col.*")
            
                        // finalDF
    val finalDF = expandedDF
            .withColumn("uuid", makeUUID())
            .withColumnRenamed("c", "trade_conditions")
            .withColumnRenamed("p", "price")
            .withColumnRenamed("s", "symbol")
            .withColumnRenamed("t","trade_timestamp")
            .withColumnRenamed("v", "volume")
            .withColumn("trade_timestamp",(col("trade_timestamp") / 1000).cast("timestamp"))
            .withColumn("ingest_timestamp",current_timestamp().as("ingest_timestamp"))

                         // write trades
    val query1 = finalDF
      .writeStream
      .foreachBatch { (batchDF:DataFrame, batchId:Long) =>
        println(s" TRADES BATCH $batchId")
          batchDF.write
            .cassandraFormat(settings.cassandra("trades"),settings.cassandra("keyspace"))
            .mode("append")
            .save()
      
      }
      .option("checkpointLocation", "/tmp/checkpoint_trades")
      .start()

                         // aggegation 
    val summaryDF = finalDF
        .withColumn("price_volume_multiply",col("price")*col("volume"))
        .withWatermark("trade_timestamp","15 seconds")
        .groupBy("symbol")
        .agg(avg("price_volume_multiply"))

    val finalsummaryDF = summaryDF
        .withColumn("uuid", makeUUID())
        .withColumn("ingest_timestamp",current_timestamp().as("ingest_timestamp"))
        .withColumnRenamed("avg(price_volume_multiply)","price_volume_multiply")

                          // write aggegation
     val query2 = finalsummaryDF
        .writeStream
        .trigger(Trigger.ProcessingTime("5 seconds"))
        .foreachBatch { (batchDF:DataFrame,batchID:Long) =>
            println(s"Writing to Cassandra $batchID")
            batchDF.write
                .cassandraFormat(settings.cassandra("aggregates"),settings.cassandra("keyspace"))
                .mode("append")
                .save()
        }
        .outputMode("update")
        .start()
        
        // let query await termination
        spark.streams.awaitAnyTermination()
  }
}