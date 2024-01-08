package object monad {

  /**
   * Реализуйте методы map / flatMap / withFilter чтобы работал код и законы монад соблюдались
   * HINT: для проверки на пустой элемент можно использовать eq
   */

  sealed abstract class Wrap[+A] {
    self =>

    def get: A

    def pure[R](x: R): Wrap[R] = NonEmptyWrap(x)

    def flatMap[R](f: A => Wrap[R]): Wrap[R] = {
      self match {
        case EmptyWrap => EmptyWrap
        case NonEmptyWrap(result) => f(result)
      }
    }

    // HINT: map можно реализовать через pure и flatMap
    def map[R](f: A => R): Wrap[R] = {
      val mapFunc = f andThen pure
      self.flatMap(mapFunc)
    }

    def withFilter(f: A => Boolean): Wrap[A] = {
      self.flatMap {
        case satisfied if f(satisfied) => NonEmptyWrap(satisfied)
        case _ => EmptyWrap
      }
    }

  }

  object Wrap {
    def empty[R]: Wrap[R] = EmptyWrap
  }

  case class NonEmptyWrap[A](result: A) extends Wrap[A] {
    override def get: A = result
  } // pure

  case object EmptyWrap extends Wrap[Nothing] {
    override def get: Nothing = throw new NoSuchElementException("Wrap.get")
  } // bottom, null element

}