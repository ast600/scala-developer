package http4sdsl.homework

import cats.effect.IO
import fs2.Stream

import scala.concurrent.duration.FiniteDuration

object StreamWorkflow {
  def simulateServerUnderLoad(chunkSize: Int, totalBytes: Int, everyNSecs: FiniteDuration): Stream[IO, String] =
    Stream.emits { LazyList.fill(totalBytes)(1.toByte) }
          .chunkN(chunkSize)
          .evalMapChunk { d => IO.sleep(everyNSecs) *> IO(d.toList.mkString) }

}
