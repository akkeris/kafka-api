package services

import java.time.Duration
import java.util.UUID

import daos.MigrationDao
import javax.inject.Inject
import models.http.HttpModels.{MigrationStage1Request, MigrationStage2Request}
import models.Models.ONE_DAY_MS
import org.apache.kafka.clients.admin.{Config, ConfigEntry}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.{ConfigResource, TopicConfig}
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import play.api.Logger
import play.api.db.Database
import utils.{AdminClientUtil, ConsumerUtil}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

class MigrationService @Inject() (
                                   db:            Database,
                                   dao:           MigrationDao,
                                   util:          AdminClientUtil,
                                   consumerUtil:  ConsumerUtil
                                 ) {

  val logger = Logger(this.getClass)

  def migrateStage1(request: MigrationStage1Request): Unit = {
    logger.info(s"MigrationStage1: Stage 1 migration started ")
    val topics = getMigration1Topics(request)
    logger.info(s"MigrationStage1: Topics to migrate: $topics")
    migration1AlterTopicConfigs(request.cluster, topics)
    for(topic <- topics) {
      val t = System.currentTimeMillis()
      produceLatestRecords(request.cluster, topic)
      db.withConnection { implicit conn =>
        dao.changeStatus(topic, "stage1_complete")
      }
      logger.info(s"MigrationStage1: Time to migrate $topic is ${System.currentTimeMillis() - t}ms")
    }
    logger.info(s"MigrationStage1: Complete migration for topics: $topics")
  }

  def getMigration1Topics(request: MigrationStage1Request): List[String] = {
    if (request.topics.isDefined) {
      db.withConnection { implicit conn =>
        dao.insertStage1MigrationTopicsToMigrationTable(request.cluster, request.topics.get);
      }
    } else {
      db.withConnection { implicit conn =>
        dao.insertStage1MigrationTopicsToMigrationTable(request.cluster);
      }
    }
  }

  def migration1AlterTopicConfigs(cluster: String, topics: List[String]): Unit = {
    val adminClient = util.getAdminClient(cluster)
    val configList = for (topic <- topics) yield {
      val configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic)

      val configs = List(
        new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, "1000"),
        new ConfigEntry(TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG, "0"),
        new ConfigEntry(TopicConfig.SEGMENT_MS_CONFIG, "1000"),
        new ConfigEntry(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT),
      )

      (configResource ->  new Config(configs.asJava))
    }
    val result = adminClient.alterConfigs(configList.toMap.asJava).all()
    adminClient.close()
    Try(result.get) match {
      case Success(_) =>
        true
      case Failure(e) =>
        logger.error(s"MigrationStage1: migration failed to change topic config: ${e.getMessage}", e)
        throw e
    }
  }

  def produceLatestRecords(cluster: String, topic: String): Unit = {
    val consumerGroupName = "kafka-api-migration1" + UUID.randomUUID.toString
    val props = consumerUtil.getKafkaProps(cluster,
      Some(List(topic)),
      consumerGroupName)

    val consumer = new KafkaConsumer[Array[Byte], Array[Byte]](props, new ByteArrayDeserializer(), new ByteArrayDeserializer())
    val producer = new KafkaProducer[Array[Byte], Array[Byte]](props, new ByteArraySerializer(), new ByteArraySerializer())

    consumer.subscribe(List(topic).asJava)
    val partitions = consumer.partitionsFor(topic).asScala.map(p => new TopicPartition(p.topic(), p.partition()))
    consumer.unsubscribe()
    consumer.assign(partitions.asJava)
    consumer.poll(Duration.ofMillis(100))
    val endOffsetsMap = consumer.endOffsets(partitions.asJava).asScala

    endOffsetsMap.foreach {case (p, eo) =>

      val offsetToSeek = if (eo > 50) eo - 50 else 0;
      consumer.seek(p, offsetToSeek);
    }

    val producerRecords = new ListBuffer[ProducerRecord[Array[Byte], Array[Byte]]]()
    val ct = System.currentTimeMillis()
    while ( System.currentTimeMillis() - ct < 3000  ) {
      val records = consumer.poll(Duration.ofSeconds(1))
      records.forEach( record => {
        producerRecords += new ProducerRecord[Array[Byte], Array[Byte]](topic, record.key(), record.value());
      })
    }

    val list = producerRecords.toList
    logger.info(s"MigrationStage1: Producing ${list.size} records to $topic")
    for (p <- list) {
      producer.send(p).get()
    }
    logger.info(s"MigrationStage1: Done producing ${list.size} records to $topic")
    consumer.close()
    producer.close()
    util.deleteConsumerGroup(cluster, consumerGroupName)
  }

  def migrateStage2(request: MigrationStage2Request) = {
    val topics = db.withConnection { implicit conn =>
      if (request.topics.isDefined && request.topics.get.size > 0)
        dao.getTopicsForStage2Migration(request.topics.get)
      else
        dao.getTopicsForStage2Migration()
    }

    logger.info(s"MigrationStage2: Starting migration for topics ${topics}")

    val adminClient = util.getAdminClient(request.cluster)

    val configList = for (topic <- topics) yield {
      val configResource = new ConfigResource(ConfigResource.Type.TOPIC, topic)

      val configs = List(
        new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, ONE_DAY_MS.toString),
        new ConfigEntry(TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG, "0.5"),
        new ConfigEntry(TopicConfig.SEGMENT_MS_CONFIG, ONE_DAY_MS.toString),
        new ConfigEntry(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT),
      )

      (configResource ->  new Config(configs.asJava))
    }
    val result = adminClient.alterConfigs(configList.toMap.asJava).all()
    adminClient.close()
    Try(result.get) match {
      case Success(_) =>
        true
      case Failure(e) =>
        logger.error(s"MigrationStage2:  failed to changetopic config: ${e.getMessage}", e)
        throw e
    }

    logger.info(s"MigrationStage2: updating database for migration status")
    db.withConnection { implicit conn =>
      dao.changeStatus(topics, "stage2_complete")
    }
    logger.info(s"MigrationStage2: complete migration for topics ${topics}")
  }

}
