import scala.collection.immutable
import akka.stream._
import akka.stream.scaladsl._
import akka.{NotUsed, Done}
import akka.actor.ActorSystem
import org.heathkang.scala3_stream.mqttSource.mqttSource
import akka.stream.alpakka.mqtt.MqttMessage
import scala.concurrent.Future
import io.circe._
import io.circe.parser._
import zio._
import zio.console._
import zio.duration.durationInt


object MyApp extends zio.App {
  // given ActorSystem = ActorSystem("Stream-Start")

  def run(args: List[String]) = 
    val runtime = Runtime.default
  
    val runGraph: RunnableGraph[SinkQueueWithCancel[Json]] = mqttSource.mqttSource.via(createFlow).toMat(toSink)(Keep.right)
    
    (for {
        materialisedValue <- runAkkaStreamGraphEffect(runGraph).provideLayer(materializerLayer).fork
                _                     <- ZIO.sleep(10.seconds)
        valueFuture             <- materialisedValue.join
        value                  <- ZIO.fromFuture( _ => valueFuture.pull )

        _                     <- putStrLn( value.toString)
    } yield ()).exitCode
  
  // def fromFutureToTask(future: Future[immutable.Seq[Json]]): UIO[Task[immutable.Seq[Json]]] = 
  //   ZIO.fromFuture { 
  //   }
  
  def runAkkaStreamGraphEffect[M](runGraph: RunnableGraph[M]): ZIO[Has[Materializer], Throwable, M] =
    for {
      mat <- ZIO.access[Has[Materializer]](_.get)
      materialisedValue <- ZIO.effectAsync(runGraph.run()(mat))
      // materialisedValue <- ZIO.fromFuture(_ => materialisedFuture)
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

  def toSink: Sink[Json, SinkQueueWithCancel[Json]] =
    Sink.queue

}