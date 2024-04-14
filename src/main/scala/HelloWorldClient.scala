import io.grpc.{ManagedChannel, ManagedChannelBuilder, StatusRuntimeException}
import yaniv.protos.learn.{HelloRequest, HelloResponse, HelloWorldGrpc}
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


// This is a companion object
object HelloWorldClient {
  // The apply method acts here as a factory method
  def apply(host: String, port: Int): HelloWorldClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build
    val blockingStub = HelloWorldGrpc.blockingStub(channel)
    val asyncStub = HelloWorldGrpc.stub(channel)

    new HelloWorldClient(channel, blockingStub, asyncStub)
  }

  def run(args: Array[String]): Unit = {
    val client = HelloWorldClient.apply("localhost", 50051)
    try {
      val user = args.headOption.getOrElse("world")
      client.greet(user)
      client.greetAsync(user).map(response => println(s"async response: ${response.greeting}"))
    } finally {
      client.shutdown()
    }
  }
}

class HelloWorldClient private(private val channel: ManagedChannel,
                               private val blockingStub: HelloWorldGrpc.HelloWorldBlockingStub,
                               private val asyncStub: HelloWorldGrpc.HelloWorldStub) {

  def shutdown(): Unit = {
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  /** Say hello to server. */
  def greet(name: String): Unit = {
    println("Will try to greet " + name + " ...")
    val request1 = HelloRequest(name, List(1, 2))
    val request2 = HelloRequest(name)
    val request3 = HelloRequest(name, List(1, 2), Map("1" -> 2, "3" -> 4))
    try {

      // blocking calls
      val response1 = blockingStub.hello(request1)
      println("Greeting1:" + response1.greeting)

      val response2 = blockingStub.hello(request2)
      println("Greeting2:" + response2.greeting)

      val response3 = blockingStub.hello(request3)
      println("Greeting3:" + response3.greeting)
    }
    catch {
      case e: StatusRuntimeException =>
        println("error")
    }
  }

  def greetAsync(name: String): Future[HelloResponse] = {
    println("Will try to greet async " + name + " ...")
    val request1 = HelloRequest(name, List(1, 2))
    asyncStub.hello(request1)
  }
}