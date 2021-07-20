package org.heathkang.scala3_stream.mqttSource

import akka.stream._
import akka.stream.scaladsl._
import akka.stream.alpakka.mqtt._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import akka.stream.alpakka.mqtt.scaladsl._
import scala.concurrent.Future
import akka.Done
import scala.compiletime.ops.string

object mqttSource {
  val topic = "sites/virtual-factory/devices/VF3000/operational-data"
  val connectionSettings = MqttConnectionSettings(
    "tcp://127.0.0.1:11883",
    "test-scala-client1",
    new MemoryPersistence
    ).withAutomaticReconnect(true)
  
  val mqttSource: Source[MqttMessage, Future[Done]] = 
    MqttSource.atMostOnce(
      connectionSettings.withClientId(clientId = "scala3-stream-mqtt"),
      MqttSubscriptions(Map(topic -> MqttQoS.AtLeastOnce)),
      bufferSize = 10
    )
}