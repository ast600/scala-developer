package module1.futures

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object task_futures_sequence {

  /**
   * В данном задании Вам предлагается реализовать функцию fullSequence,
   * похожую на Future.sequence, но в отличии от нее,
   * возвращающую все успешные и не успешные результаты.
   * Возвращаемое тип функции - кортеж из двух списков,
   * в левом хранятся результаты успешных выполнений,
   * в правово результаты неуспешных выполнений.
   * Не допускается использование методов объекта Await и мутабельных переменных var
   */
  /**
   * @param futures список асинхронных задач
   * @return асинхронную задачу с кортежом из двух списков
   */
  def fullSequence[A](futures: List[Future[A]])
                     (implicit ex: ExecutionContext): Future[(List[A], List[Throwable])] =
    futures.foldLeft( Future.successful { (List.empty[A], List.empty[Throwable]) } ) {
      (futAcc, future) => future.transformWith {
        case Success(value) =>
          futAcc.map { case (values, errors) => (value +: values, errors) }
        case Failure(exception) =>
          futAcc.map { case (values, errors) => (values, exception +: errors) }
      }
    }.map { case (reversedValues, reversedExceptions) => (reversedValues.reverse, reversedExceptions.reverse) }
}
