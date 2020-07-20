package e2e

import java.time.Duration
import java.util.{ Properties, UUID }

import com.github.javafaker.Faker
import com.sksamuel.avro4s.ToRecord
import io.confluent.kafka.serializers.{ KafkaAvroDeserializer, KafkaAvroSerializer }
import org.apache.avro.generic.GenericRecord
import org.apache.avro.reflect.AvroName
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ ConsumerConfig, KafkaConsumer }
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerConfig, ProducerRecord }
import org.apache.kafka.common.config.SaslConfigs
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Logger
import utils.AdminClientUtil.ADMIN_CLIENT_ID

import scala.collection.JavaConverters._

class ProducerTests extends PlaySpec with ScalaFutures with IntegrationPatience with MockitoSugar with AbstractSpec {

  val topic = "test-state-topic-13"
  val topicKey = s"$topic-key"
  val topicValue = s"$topic-value"
  val logger = Logger(this.getClass)

  //  "Topic Producer" must {
  //    "produce to topic" in {
  //
  //      val faker = new Faker()
  //      val r = scala.util.Random
  //
  //      val kafkaHostName = "localhost:9092"
  //      val avroLocation = "http://localhost:8081"
  //
  //      val props = new Properties()
  //      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHostName)
  //      props.put(ConsumerConfig.CLIENT_ID_CONFIG, s"${ADMIN_CLIENT_ID}-${UUID.randomUUID.toString}")
  //      props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1")
  //      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  //      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  //
  //      props.put("schema.registry.url", avroLocation)
  //      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer].getName)
  //      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer].getName)
  //      props.put("key.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy")
  //      props.put("value.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy")
  //      val producer = new KafkaProducer[GenericRecord, GenericRecord](props)
  //
  //      implicit val ProducerToRecord: ToRecord[ValueProducerRecord] = ToRecord[ValueProducerRecord]
  //      implicit val IdToRecord: ToRecord[Id] = ToRecord[Id]
  //
  //      for (i <- 1 to 100000) {
  //        if (i % 1000 == 0)
  //          println(s"running iteration $i")
  //        val id = Id(r.nextInt(100))
  //        val record = ValueProducerRecord(id.id, faker.name().fullName())
  //
  //        val value: GenericRecord = ProducerToRecord.to(record)
  //        val key: GenericRecord = IdToRecord.to(id)
  //
  //        val producerRecord = new ProducerRecord[GenericRecord, GenericRecord](topic, key, value)
  //        producer.send(producerRecord).get()
  //      }
  //      producer.close();
  //    }
  //  }

  //  "Consumer Test" must {
  //    "consume messages" in {
  //      val props = new Properties()
  //      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  //      props.put(ConsumerConfig.CLIENT_ID_CONFIG, s"TestingConsumer-${UUID.randomUUID.toString}")
  //      props.put(ConsumerConfig.GROUP_ID_CONFIG, "idp-core-qa")
  //      props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1")
  //      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  //      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  //
  //      props.put("schema.registry.url", "http://localhost:8081")
  //      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[KafkaAvroDeserializer].getName)
  //      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[KafkaAvroDeserializer].getName)
  //      props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
  //      props.put(SaslConfigs.SASL_MECHANISM, "PLAIN")
  //      props.put(SaslConfigs.SASL_JAAS_CONFIG,
  //        s"""org.apache.kafka.common.security.plain.PlainLoginModule required username="abc" password="***";""")
  //
  //      println(props)
  //      val consumer = new KafkaConsumer[GenericRecord, GenericRecord](props)
  //      consumer.subscribe(List("qa-core-identity").asJava)
  //      val t = System.currentTimeMillis()
  //      println(s"starting to consume. Subscriptions ${consumer.subscription().asScala}")
  //
  //      while(System.currentTimeMillis() - t < 3000) {
  //        println("consuming")
  //        val records = consumer.poll(Duration.ofSeconds(1))
  ////        println(s"consumed ${records.count()} messages")
  //        records.forEach( record => {
  //          println(s"key : ${record.key()}, value: ${record.value()}")
  //        })
  //      }
  //      consumer.close()
  //    }
  //  }
  @AvroName("test-state-topic-2-key")
  case class Id(id: Int)

  @AvroName("test-state-topic-2-value")
  case class ValueProducerRecord(
      id:   Long,
      name: String
  );
}
