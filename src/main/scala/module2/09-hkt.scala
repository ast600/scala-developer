package module2

object higher_kinded_types {

  def tuple[A, B](a: List[A], b: List[B]): List[(A, B)] =
    a.flatMap { a => b.map((a, _)) }

  def tuple[A, B](a: Option[A], b: Option[B]): Option[(A, B)] =
    a.flatMap { a => b.map((a, _)) }

  def tuple[E, A, B](a: Either[E, A], b: Either[E, B]): Either[E, (A, B)] =
    a.flatMap { a => b.map((a, _)) }


  def tupleF[F[_], A, B](fa: F[A], fb: F[B])(implicit b: Bind[F]): F[(A, B)] = {
    b.flatMap(fa) { a => b.map(fb) { (a, _) } }
  }

  trait Bind[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]

    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  object Bind {
    implicit val OptionBind: Bind[Option] = new Bind[Option] {

      override def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa.map { f }

      override def flatMap[A, B](fa: Option[A])
                                (f: A => Option[B]): Option[B] = fa.flatMap { f }
    }

    implicit val ListBind: Bind[List] = new Bind[List] {
      override def map[A, B](fa: List[A])
                            (f: A => B): List[B] = fa.map { f }

      override def flatMap[A, B](fa: List[A])
                                (f: A => List[B]): List[B] = fa.flatMap { f }
    }
  }

  trait Bindable[F[_], A] {
    def map[B](f: A => B): F[B]

    def flatMap[B](f: A => F[B]): F[B]
  }

  def tupleBindable[F[_], A, B](fa: Bindable[F, A], fb: Bindable[F, B]): F[(A, B)] =
    fa.flatMap(a => fb.map(b => (a, b)))

  def optBindable[A](opt: Option[A]): Bindable[Option, A] = new Bindable[Option, A] {
    override def map[B](f: A => B): Option[B] = opt.map(f)

    override def flatMap[B](f: A => Option[B]): Option[B] = opt.flatMap(f)
  }

  def listBindable[A](list: List[A]): Bindable[List, A] = new Bindable[List, A] {
    override def map[B](f: A => B): List[B] = list.map { f }

    override def flatMap[B](f: A => List[B]): List[B] = list.flatMap { f }
  }


  val optA: Option[Int] = Some(1)
  val optB: Option[Int] = Some(2)

  val list1 = List(1, 2, 3)
  val list2 = List(4, 5, 6)

  val r3: Option[(Int, Int)] = tupleBindable(optBindable(optA), optBindable(optB))
  val r4 = println(tupleBindable(listBindable(list1), listBindable(list2)))


  lazy val r1 = println(tupleF(optA, optB))
  lazy val r2 = println(tupleF(list1, list2))

}