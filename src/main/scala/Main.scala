import akka.stream._
import akka.stream.scaladsl._
import akka.{NotUsed, Done}
import akka.actor.ActorSystem
import org.heathkang.scala3_stream.mqttSource.mqttSource
import akka.stream.alpakka.mqtt.MqttMessage
import scala.concurrent.Future
import zio.{ Has, Layer, Managed, Task, ZLayer }
import com.github.ekeith.zio.akkastream.Converters.runnableGraphAsZioEffect
import org.json4s
import org.json4s.native.JsonMethods._
import org.json4s.JValue


@main def hello: Unit = 
  given ActorSystem = ActorSystem("Stream-Start")
  createSource.runForeach(i => println(i))
  val runGraph: RunnableGraph[Future[Done]] = mqttSource.mqttSource.via(createFlow).toMat(toSink)(Keep.right)
  runGraph.run()

def msg = "I was compiled by Scala 3. :)"

def createSource: Source[Int, NotUsed] = 
  Source(1 to 10)

def createFlow: Flow[MqttMessage, JValue, NotUsed] = 
  Flow[MqttMessage].map(
    mqttMessage => parse(mqttMessage.payload.utf8String)
  )

def toSink: Sink[JValue, Future[Done]] =
  Sink.foreach(s => println(compact(render(s))))
