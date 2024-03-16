package http4sdsl.homework

import cats.data.{ Kleisli, NonEmptyList }
import cats.effect.kernel.Ref
import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits.catsSyntaxTuple3Semigroupal
import com.comcast.ip4s.IpLiteralSyntax
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto.autoUnwrap
import eu.timepit.refined.numeric.Positive
import http4sdsl.homework.Params.{ ChunkSizeQueryParam, PeriodInSecsQueryParam, TotalBytesQueryParam }
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import org.http4s.Method.GET
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.{ HttpRoutes, ParseFailure, Request, Response }

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

object CounterServer extends IOApp {
  private[homework] def getCounterRoutes(counterRef: Ref[IO, Int]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "counter" =>
      for {
        upd <- counterRef.updateAndGet { _ + 1 }
        resp <- Ok(Counter(upd).asJson)
      } yield resp
    case GET -> Root / "slow" :? ChunkSizeQueryParam(chunkSize) +& TotalBytesQueryParam(
    totalBytes) +& PeriodInSecsQueryParam(period) =>
      (chunkSize, totalBytes, period).tupled
                                     .fold(handleParseErrors, proceedToStreaming.tupled)
  }

  private val handleParseErrors: NonEmptyList[ParseFailure] => IO[Response[IO]] = {
    failures =>
      val errorString = failures.map { _.sanitized }.reduce { (_: String) + "," + (_: String) }
      BadRequest(s"An error occurred while making the request: $errorString")
  }

  private val proceedToStreaming: (Int Refined Positive, Int Refined Positive, FiniteDuration) => IO[Response[IO]] =
    (c, b, p) => Ok(StreamWorkflow.simulateServerUnderLoad(c, b, p))


  private val httpApp: IO[Kleisli[IO, Request[IO], Response[IO]]] =
    for {
      ref <- Ref.of[IO, Int](0)
      counterService = getCounterRoutes(ref)
      counterApp = Router("/" -> counterService)
    } yield counterApp.orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    for {
      application <- httpApp
      server <- EmberServerBuilder.default[IO]
                                  .withHost(ipv4"0.0.0.0")
                                  .withPort(port"8080")
                                  .withHttpApp(application)
                                  .withIdleTimeout(10.seconds)
                                  .build
                                  .use(_ => IO.never)
                                  .as(ExitCode.Success)
    } yield server
}
