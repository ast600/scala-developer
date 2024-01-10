package module1

import java.util.UUID
import scala.annotation.tailrec
import java.time.Instant
import scala.language.postfixOps


/**
 * referential transparency
 */


object referential_transparency {

  case class Abiturient(id: String, email: String, fio: String)

  type Html = String

  sealed trait Notification

  object Notification {
    case class Email(email: String, text: Html) extends Notification

    case class Sms(telephone: String, msg: String) extends Notification
  }


  case class AbiturientDTO(email: String, fio: String, password: String)

  trait NotificationService {
    def sendNotification(notification: Notification): Unit

    def createNotification(abiturient: Abiturient): Notification
  }


  trait AbiturientService {

    def registerAbiturient(abiturientDTO: AbiturientDTO): Abiturient
  }

}


// recursion

object recursion {

  /**
   * Реализовать метод вычисления n!
   * n! = 1 * 2 * ... n
   */

  def fact(n: Int): Int = {
    var _n = 1
    var i = 2
    while (i <= n) {
      _n *= i
      i += 1
    }
    _n
  }

  def factRec(n: Int): Int = if (n == 0) 1 else n * factRec(n - 1)

  def tailRec(n: Int): Int = {
    @tailrec
    def loop(i: Int, accum: Int): Int = if (i == 0) accum else loop(i - 1, n * accum)

    loop(n, 1)
  }


  /**
   * реализовать вычисление N числа Фибоначчи
   * F0 = 0, F1 = 1, Fn = Fn-1 + Fn - 2
   *
   */


}

object hof {


  // обертки

  def logRunningTime[A, B](f: A => B): A => B = a => {
    val start = System.currentTimeMillis()
    val result = f(a)
    val end = System.currentTimeMillis()
    println(end - start)
    result
  }

  def doomy(string: String) = {
    Thread.sleep(1000)
    println(string)
  }



  // изменение поведения ф-ции

  val arr = Array(1, 2, 3, 4, 5)

  def isOdd(i: Int): Boolean = i % 2 > 0

  val isEven: Int => Boolean = not(isOdd)

  def not[A](f: A => Boolean): A => Boolean = a => !f(a)


  // изменение самой функции

  def sum(x: Int, y: Int): Int = x + y

  val s1: Int => Int = partial(5, sum)

  s1(3) // 8

  def partial[A, B, C](a: A, f: (A, B) => C): B => C = b => f(a, b)

  def partial2[A, B, C](a: A, f: (A, B) => C): B => C = f.curried(a)


  trait Consumer {
    def subscribe(topic: String): LazyList[Record]
  }

  case class Record(value: String)

  case class Request()

  object Request {
    def parse(str: String): Request = ???
  }

  /**
   *
   * (Опционально) Реализовать ф-цию, которая будет читать записи Request из топика,
   * и сохранять их в базу
   */
  def createRequestSubscription() = ???


}


/**
 * Реализуем тип Option
 */


object opt {

  /**
   *
   * Реализовать структуру данных Option, который будет указывать на присутствие либо отсутсвие результата
   */

  // + covariant Option[Animal] родитель для Option[Dog]
  // invariant Option[Animal] нет связи Option[Dog]
  // - contravariant связь наоборот между Option[Animal] и Option[Dog]

  class Animal

  class Dog extends Animal

  def findAnimal: Option[Animal] = ???

  def findDog: Option[Dog] = ???

  def treat(animal: Animal): Unit = ???

  def treat(animal: Option[Animal]): Unit = ???

  val animal: Animal = ???
  val dog: Dog = ???
  treat(animal)
  treat(dog)

  def divide(x: Int, y: Int): Option[Int] = {
    if (y == 0) None
    else Some(x / y)
  }

  sealed trait Option[+T] {

    def isEmpty: Boolean = this match {
      case None => true
      case Some(v) => false
    }

    def get: T = this match {
      case Some(v) => v
      case None => throw new Exception("get on empty Option")
    }

    def map[B](f: T => B): Option[B] = flatMap(v => Option(f(v)))

    def flatMap[B](f: T => Option[B]): Option[B] = this match {
      case Some(v) => f(v)
      case None => None
    }
  }

  val opt: Option[Int] = ???

  val opt2: Option[Int] = opt.flatMap(i => Option(i + 1))
  val opt3 = opt.map(i => i + 1)

  case class Some[T](v: T) extends Option[T]

  case object None extends Option[Nothing]

  object Option {
    def apply[T](v: T): Option[T] = Some(v)
  }


  /**
   *
   * Реализовать метод printIfAny, который будет печатать значение, если оно есть
   */

  def printIfAny[A](inOption: Option[A]): Unit = {
    inOption match {
      case Some(v) => println(v)
      case None => ()
    }
  }

  /**
   *
   * Реализовать метод zip, который будет создавать Option от пары значений из 2-х Option
   */
  
  def zip[A, B](optLeft: Option[A], optRight: Option[B]): Option[(A, B)] = {
    (optLeft, optRight) match {
      case (Some(v1), Some(v2)) => Option((v1, v2))
      case _ => None
    }
  }


  /**
   *
   * Реализовать метод filter, который будет возвращать не пустой Option
   * в случае если исходный не пуст и предикат от значения = true
   */

  def filter[T](inOption: Option[T])(p: T => Boolean): Option[T] = {
    inOption match {
      case Some(v) if p(v) => inOption
      case _ => None
    }
  }
}

object list {
  /**
   *
   * Реализовать односвязанный иммутабельный список List
   * Список имеет два случая:
   * Nil - пустой список
   * Cons - непустой, содердит первый элемент (голову) и хвост (оставшийся список)
   */

  sealed trait List[+T] {
    def ::[TT >: T](elem: TT): List[TT] = {
      val newArgs = elem +: List.unapply(this)
      List(newArgs: _*)
    }
  }

  case class Cons[A](head: A, tail: List[A]) extends List[A]
  case object Nil extends List[Nothing]

  object List {

    def apply[A](v: A*): List[A] =
      if (v.isEmpty) Nil
      else Cons(v.head, apply(v.tail: _*))

    def unapply[T](inList: List[T]): Seq[T] = {
      @tailrec
      def cons2Seq[R](inCons: Cons[R], acc: Seq[R] = Seq.empty[R]): Seq[R] = {
        if (inCons.tail == Nil) {
          val reversed = inCons.head +: acc
          reversed.reverse
        }
        else cons2Seq(inCons.tail.asInstanceOf[Cons[R]], inCons.head +: acc)
      }

      inList match {
        case nonEmpty: Cons[T] => cons2Seq(nonEmpty)
        case Nil => Seq.empty[T]
      }
    }
  }

  Cons(1, Nil) // List(1)
  Cons(1, Cons(2, Nil)) // List(1, 2)
  Cons(1, Cons(2, Cons(3, Nil))) // List(1, 2, 3)


  /**
   * Метод cons, добавляет элемент в голову списка, для этого метода можно воспользоваться названием `::`
   *
   */

  // См. метод интерфейса List

  /**
   * Метод mkString возвращает строковое представление списка, с учетом переданного разделителя
   *
   */

  def mkString[T](inList: List[T], sep: String): String = {
    List.unapply(inList).mkString(sep)
  }

  /**
   * Конструктор, позволяющий создать список из N - го числа аргументов
   * Для этого можно воспользоваться *
   *
   * Например вот этот метод принимает некую последовательность аргументов с типом Int и выводит их на печать
   * def printArgs(args: Int*) = args.foreach(println(_))
   */

  /**
   *
   * Реализовать метод reverse который позволит заменить порядок элементов в списке на противоположный
   */

  def reverse[T](inList: List[T]): List[T] = {
    val reversedArgs = List.unapply(inList).reverse
    List(reversedArgs: _*)
  }

  /**
   *
   * Реализовать метод map для списка который будет применять некую ф-цию к элементам данного списка
   */

  def map[A, B](inList: List[A])(f: A => B): List[B] = {
    // Читерство

    val a = List.unapply(inList).map { f }
    List(a: _*)
  }

  /**
   *
   * Реализовать метод filter для списка который будет фильтровать список по некому условию
   */

  def filter[A](inList: List[A])(p: A => Boolean): List[A] = {
    // Читерство

    val a = List.unapply(inList).filter { p }
    List(a: _*)
  }

  def flatMap[A, B](inList: List[A])(g: A => List[B]): List[B] = {
    val nested = map(inList)(g)
    val unapplied = List.unapply(map(nested){ List.unapply })
    val flattened = unapplied reduce { _ ++ _ }
    List(flattened: _*)
  }

  /**
   *
   * Написать функцию incList котрая будет принимать список Int и возвращать список,
   * где каждый элемент будет увеличен на 1
   */

  def incList(inList: List[Int]): List[Int] = {
    map(inList) { num => num + 1 }
  }

  /**
   *
   * Написать функцию shoutString котрая будет принимать список String и возвращать список,
   * где к каждому элементу будет добавлен префикс в виде '!'
   */

  def shoutString(inList: List[String]): List[String] = {
    map(inList) { str => "!" + str }
  }
}