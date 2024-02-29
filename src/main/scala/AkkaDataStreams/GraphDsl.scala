package AkkaDataStreams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, Zip}
import akka.stream.{ActorMaterializer, ClosedShape}

object AkkaStreamsGraph {
  implicit  val system = ActorSystem("sdf")
  implicit  val matetializer = ActorMaterializer()

  val graph = GraphDSL.create(){ implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    //1. source
    val input = builder.add(Source(1 to 1000))
    val inc = builder.add(Flow[Int].map(x=>x+1))
    val multiplier = builder.add(Flow[Int].map(x=>x*10))
    val output = builder.add(Sink.foreach[(Int, Int)](println))

    val broadcast = builder.add(Broadcast[Int](2))
    val zip = builder.add(Zip[Int, Int])

    // shape
    input ~> broadcast

    broadcast.out(0) ~> inc ~> zip.in0
    broadcast.out(1) ~> multiplier ~> zip.in1
    zip.out ~> output

    //close shape
    ClosedShape
  }

  def main(args: Array[String]): Unit ={
    RunnableGraph.fromGraph(graph).run()
  }
}