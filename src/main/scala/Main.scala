import akka.stream._
import akka.stream.scaladsl._
import akka.{NotUsed, Done}
import akka.actor.ActorSystem
import org.heathkang.scala3_stream.mqttSource.mqttSource
import akka.stream.alpakka.mqtt.MqttMessage
import scala.concurrent.Future
import zio.{ZIO,Has, ZLayer, Layer, Runtime, Managed, Task}
import io.circe._
import io.circe.parser._

object MyApp extends zio.App {
  // given ActorSystem = ActorSystem("Stream-Start")

  def run(args: List[String]) = 
    val runtime = Runtime.default
  
    val runGraph: RunnableGraph[Future[Done]] = mqttSource.mqttSource.via(createFlow).toMat(toSink)(Keep.right)
    
    (for {
        materialisedValue <- runAkkaStreamGraphEffect(runGraph).provideLayer(materializerLayer)
    } yield materialisedValue).exitCode

  def runAkkaStreamGraphEffect[M](runGraph: RunnableGraph[Future[M]]): ZIO[Has[Materializer], Throwable, M] =
    for {
      mat <- ZIO.access[Has[Materializer]](_.get)
      materialisedFuture <- ZIO.effect(runGraph.run()(mat))
      materialisedValue <- ZIO.fromFuture(_ => materialisedFuture)
    } yield materialisedValue

  val actorSystem: Layer[Throwable, Has[ActorSystem]] = 
    ZLayer.fromManaged(Managed.make(Task(ActorSystem("Stream-start")))
      (sys => Task.fromFuture(_ => sys.terminate()).either))

  val materializerLayer: Layer[Throwable, Has[Materializer]] = 
    actorSystem >>> ZLayer.fromFunction(as => Materializer(as.get))

  def createFlow: Flow[MqttMessage, Json, NotUsed] = 
    Flow[MqttMessage].map(
      mqttMessage => parse(mqttMessage.payload.utf8String).getOrElse(Json.Null)
    )

  def toSink: Sink[Json, Future[Done]] =
    Sink.foreach(s => println(s))

}