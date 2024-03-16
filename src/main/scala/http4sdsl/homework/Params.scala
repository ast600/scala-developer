package http4sdsl.homework

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import org.http4s.dsl.io.ValidatingQueryParamDecoderMatcher

import scala.concurrent.duration.FiniteDuration

object Params {

  import HttpDecoders._

  object ChunkSizeQueryParam extends ValidatingQueryParamDecoderMatcher[Int Refined Positive]("chunkSize")

  object TotalBytesQueryParam extends ValidatingQueryParamDecoderMatcher[Int Refined Positive]("totalBytes")

  object PeriodInSecsQueryParam extends ValidatingQueryParamDecoderMatcher[FiniteDuration]("periodInSecs")

}
