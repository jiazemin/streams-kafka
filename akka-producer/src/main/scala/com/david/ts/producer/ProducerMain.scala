package com.david.ts.producer

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream._
import akka.stream.scaladsl.{ Source }
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer }
import org.apache.log4j.Logger

import scala.concurrent.duration._
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

object ProducerMain extends App {
  import akka.actor._
  lazy val logger = Logger.getLogger(getClass)
  logger.info(s"Starting Producer2")

  implicit val system = ActorSystem()
  implicit val mater = ActorMaterializer()

  val kafkaEndpoint = System.getProperty("kafka_endpoint", "http://192.168.99.100:30092")
  logger.info(s"Connecting to kafka endpoint $kafkaEndpoint")
  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(kafkaEndpoint).withCloseTimeout(5 minutes)

  val random = new Random()
  val s = Source
    .tick(0.seconds, 300.millis, "").map(_ => random.nextInt(1000))

  s.map { number =>
    logger.info(s"Producing record: $number")
    new ProducerRecord[Array[Byte], String]("test", s"$number")
  }.runWith(Producer.plainSink(producerSettings))
}
