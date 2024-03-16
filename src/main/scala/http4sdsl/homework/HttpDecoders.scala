package http4sdsl.homework

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import org.http4s.{ ParseFailure, QueryParamDecoder }

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

object HttpDecoders {
  implicit val PositiveNumDecoder: QueryParamDecoder[Int Refined Positive] =
    QueryParamDecoder.intQueryParamDecoder.emap { num =>
      refineV[Positive](num).left.map { err => ParseFailure("Got non-positive number", err) }
    }

  implicit val DurationSecDecoder: QueryParamDecoder[FiniteDuration] =
    QueryParamDecoder.intQueryParamDecoder.emap { numSecs =>
      refineV[Positive](numSecs).left
                                .map { err => ParseFailure("Got non-positive duration", err) }
                                .map { numWrapped => numWrapped.value.seconds }
    }
}
