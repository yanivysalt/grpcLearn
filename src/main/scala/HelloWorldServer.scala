
import yaniv.protos.learn._
import io.grpc.{Server, ServerBuilder}

import scala.concurrent.{ExecutionContext, Future}

object HelloWorldServer {

  def run(): Unit = {
    val server = new HelloWorldServer(ExecutionContext.global)
    server.start()
    // server.blockUntilShutdown()
  }

  private val port = 50051
}

class HelloWorldServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = null

  private def start(): Unit = {
    server = ServerBuilder
      .forPort(HelloWorldServer.port)
      .addService(HelloWorldGrpc.bindService(new helloWorldImpl, executionContext)).build.start

    println("Server started, listening on " + HelloWorldServer.port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class helloWorldImpl extends HelloWorldGrpc.HelloWorld {
    override def hello(req: HelloRequest): Future[HelloResponse] = {
      println("got in request:" + req.name + "|" + req.numbers + "|" + req.map)
      val reply = HelloResponse("Hello " + req.name + "!")
      Future.successful(reply)
    }
  }

}
