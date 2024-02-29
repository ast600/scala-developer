package catsresources

import cats.MonadError
import cats.data.State
import cats.implicits._
import cats.effect.{Clock, Fiber, IO, IOApp, Spawn}
import cats.effect.unsafe.implicits.global
import cats.effect.kernel._

import scala.concurrent.duration._

object catsresources {

  //monad error
  def main(args: Array[String]): Unit = {
    val optionF = for {
      a <- Some(3)
      b <- Some(3)
      c <- Some(3)
      d <- Some(3)
    } yield  a + b + c + d


    val optionF1 = for {
      a <- Right(3)
      b <- Right(3)
      c <- Left("error")
      d <- Right(3)
    } yield  a + b + c + d


    val stateFState = for {
      a <- State((s:Int) => (s+1, s))
      b <- State((s:Int) => (s+1, s))
      c <- State((s:Int) => (s+1, s))
      d <- State((s:Int) => (s+1, s))
    } yield  (s"$a + $b + $c + $d")
   // println(stateFState.runA(10).value)

    // MonadError
    type MyMonadError[F[_]] = MonadError[F, String]
    def withErrorHandling[F[_]: MyMonadError] : F[Int] = for {
      a <- MonadError[F,String].pure(10)
      b <- MonadError[F,String].pure(10)
      c <- MonadError[F,String].pure(10)
      d <- MonadError[F,String].pure(10)
    } yield (a + b + c + d)

    type StringError[A] = Either[String, A]
//    println(withErrorHandling[StringError])

    def withErrorHandling1[F[_]: MyMonadError] : F[Int] = for {
      a <- MonadError[F,String].pure(10)
      b <- MonadError[F,String].pure(10)
      c <- MonadError[F,String].raiseError[Int]("fail")
      d <- MonadError[F,String].pure(10)
    } yield (a + b + c + d)
   // println(withErrorHandling1[StringError])

    //1. handle error like try catch
    //println(withErrorHandling1.handleError(error => 42))
    def withErrorHandling2[F[_]: MyMonadError] : F[Int] = for {
      a <- MonadError[F,String].pure(10)
      b <- MonadError[F,String].pure(10)
      c <- MonadError[F,String].raiseError[Int]("fail")
        .handleError(error => 42)
      d <- MonadError[F,String].pure(10)
    } yield (a + b + c + d)
  //  println(withErrorHandling2.handleError(error => 42))

    //2. attempt

    def withErrorAttempt[F[_]: MyMonadError]: F[Either[String, Int]] = withErrorHandling1[F].attempt
    val nonFailing = IO.raiseError(new Exception("fail")).attempt
    val failing = IO.raiseError(new Exception("fail"))
    failing *> IO.println("111")

    //monad cancel

    val justSleep = IO.sleep(1.second) *> IO.println("not cancelled")
    val justSleepAndThrow = IO.sleep(100.millis) *> IO.raiseError(new Exception("error"))
//    (justSleep, justSleepAndThrow).parTupled.unsafeRunSync()

    val justSleepAndThrowUncancellable = (IO.sleep(1.second) *> IO.println("not cancelled")).uncancelable
    (justSleepAndThrow, justSleepAndThrowUncancellable).parTupled.unsafeRunSync()
  }

}


object SpawnApp extends  IOApp.Simple {

  def longRunningIO(): IO[Unit] =
    (
      IO.sleep(200.millis) *> IO.println(s"hi from thread ${Thread.currentThread}").iterateWhile(_ => true)
    )

  def longRunningIORef(r: Ref[IO, Int]): IO[Unit] =
    (
      IO.sleep(200.millis) *> IO.println(s"hi from thread ${Thread.currentThread}").iterateWhile(_ => true)
      )

/*
  def run: IO[Unit] = for {
    r <- Ref.of[IO, Int](10)
    fiber1 <- Spawn[IO].start(longRunningIORef(r))
    fiber2 <- Spawn[IO].start(longRunningIO)
    fiber3 <- Spawn[IO].start(longRunningIO)

    _ <- IO.println("the fibers has been started")
    _ <- IO.sleep(2.second)

    _ <- fiber1.cancel
    _ <- fiber2.cancel
    _ <- IO.sleep(3.second)
  } yield ()*/

  def run: IO[Unit] = for {
    fiber <- Spawn[IO].start(longRunningIO())
    _ <- IO.println("the fibers has been started")
    _ <- IO.sleep(1.second)

  } yield ()
}