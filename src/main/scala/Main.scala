import akka.stream._
import akka.stream.scaladsl._
import akka.{NotUsed, Done}
import akka.actor.ActorSystem
import org.heathkang.scala3_stream.mqttSource.mqttSource
import akka.stream.alpakka.mqtt.MqttMessage
import scala.concurrent.Future
import zio._
import com.github.ekeith.zio.akkastream.Converters.runnableGraphAsZioEffect
import org.json4s
import org.json4s.native.JsonMethods._
import org.json4s.JValue

object MyApp extends zio.App {
  // given ActorSystem = ActorSystem("Stream-Start")

  def run(args: List[String]) = 
    val runtime = Runtime.default
  
    val runGraph: RunnableGraph[Future[Done]] = mqttSource.mqttSource.via(createFlow).toMat(toSink)(Keep.right)
    
    (for {
        materialisedValue <- runnableGraphAsZioEffect(runGraph).provideLayer(materializerLayer)
    } yield materialisedValue).exitCode
  // runGraph.run()

  val actorSystem: Layer[Throwable, Has[ActorSystem]] = 
    ZLayer.fromManaged(Managed.make(Task(ActorSystem("Stream-start")))
      (sys => Task.fromFuture(_ => sys.terminate()).either))

  val materializerLayer: Layer[Throwable, Has[Materializer]] = 
    actorSystem >>> ZLayer.fromFunction(as => Materializer(as.get))

  def createFlow: Flow[MqttMessage, JValue, NotUsed] = 
    Flow[MqttMessage].map(
      mqttMessage => parse(mqttMessage.payload.utf8String)
    )

  def toSink: Sink[JValue, Future[Done]] =
    Sink.foreach(s => println(compact(render(s))))

}