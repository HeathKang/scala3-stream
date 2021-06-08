import akka.stream._
import akka.stream.scaladsl._
import akka.{NotUsed, Done}
import akka.actor.ActorSystem
import org.heathkang.scala3_stream.mqttSource.mqttSource
import akka.stream.alpakka.mqtt.MqttMessage
import scala.concurrent.Future


@main def hello: Unit = 
  println("Hello world!")
  println(msg)
  given ActorSystem = ActorSystem("Stream-Start")
  createSource.runForeach(i => println(i))
  val runGraph: RunnableGraph[Future[Done]] = mqttSource.mqttSource.via(createFlow).toMat(toSink)(Keep.right)
  runGraph.run()

def msg = "I was compiled by Scala 3. :)"

def createSource: Source[Int, NotUsed] = 
  Source(1 to 10)

def createFlow: Flow[MqttMessage, String, NotUsed] = 
  Flow[MqttMessage].map(
    mqttMessage => mqttMessage.payload.utf8String
  )

def toSink: Sink[String, Future[Done]] =
  Sink.foreach(s => println(s))
