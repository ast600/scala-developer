package http4sdsl.homework

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.text.utf8
import http4sdsl.homework.CounterServer.getCounterRoutes
import io.circe.generic.auto.exportDecoder
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.{ Method, Request }
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers


class CounterSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  private val TestClient =
    for {
      underlyingRef <- Ref.of[IO, Int](0)
      app = getCounterRoutes(underlyingRef)
    } yield Client.fromHttpApp[IO](app.orNotFound)

  { "Counter" } - {
    "should be incremented after sending a request" in {
      val tc = for {
        client <- TestClient
        req = Request[IO](method = Method.GET, uri = uri"/counter")
        testCounter <- client.expect[Counter](req)
      } yield testCounter

      tc.asserting { c => c.count shouldBe 1 }
    }
  }

  { "Server under load (slow)" } - {
    "should return a string separated in chunks with a predictable length" in {
      val ts = for {
        client <- TestClient
        req = Request[IO](method = Method.GET, uri = uri"/slow?chunkSize=5&totalBytes=10&periodInSecs=3")
        resp <- client.run(req).use(_.body.through(utf8.decode).compile.string)
      } yield resp

      ts.asserting { str => str shouldBe "\"11111\"\"11111\"" }
    }
  }
}
