package module3

import module3.zio_homework.config.AppConfig
import zio.ZIO
import zio.clock.{ Clock, currentTime, sleep }
import zio.console.{ Console, getStrLn, putStrLn, putStrLnErr }
import zio.duration.Duration
import zio.random.{ Random, nextIntBetween, setSeed }

import scala.concurrent.duration.{ DurationInt, MILLISECONDS }
import scala.language.postfixOps
import scala.util.Try

package object zio_homework {
  /**
   * 1.
   * Используя сервисы Random и Console, напишите консольную ZIO программу которая будет предлагать пользователю угадать число от 1 до 3
   * и печатать в консоль угадал или нет. Подумайте, на какие наиболее простые эффекты ее можно декомпозировать.
   */


  lazy val guessProgram: ZIO[Console with Random, Throwable, Boolean] =
    for {
      _ <- setSeed(4)
      numToGuess <- nextIntBetween(1, 11)
      _ <- putStrLn("Try to guess my number:")
      userInputStr <- getStrLn
      tryParse = Try { Integer.parseInt(userInputStr) }
      userInt <- ZIO.fromTry { tryParse }
    } yield numToGuess == userInt

  /**
   * 2. реализовать функцию doWhile (общего назначения), которая будет выполнять эффект до тех пор, пока его значение в условии не даст true
   *
   */

  def doWhile[R, E, A](effect: ZIO[R,E,A])(inc: A => Boolean): ZIO[R, E, A] =
    effect.repeatWhile{ inc }

  /**
   * 3. Реализовать метод, который безопасно прочитает конфиг из файла, а в случае ошибки вернет дефолтный конфиг
   * и выведет его в консоль
   * Используйте эффект "load" из пакета config
   */


  def loadConfigOrDefault: ZIO[Console, Nothing, AppConfig] = {
    val defaultConfig = AppConfig("intranet.com", "443")
    val fallbackEffect = for {
      conf <- ZIO.succeed(defaultConfig)
      _ <- putStrLnErr("Unable to read configurations from file, falling back to defaults")
      _ <- putStrLnErr(s"Default host is `${conf.host}`")
      _ <- putStrLnErr(s"Default port is `${conf.port}`")
    } yield conf

    config.load.orElse { fallbackEffect }
  }


  /**
   * 4. Следуйте инструкциям ниже для написания 2-х ZIO программ,
   * обратите внимание на сигнатуры эффектов, которые будут у вас получаться,
   * на изменение этих сигнатур
   */


  /**
   * 4.1 Создайте эффект, который будет возвращать случайеым образом выбранное число от 0 до 10 спустя 1 секунду
   * Используйте сервис zio Random
   */
  lazy val eff: ZIO[Random with Clock, Nothing, Int] =
    for {
      _ <- setSeed(4)
      _ <- sleep(Duration.fromScala(1.seconds))
      num <- nextIntBetween(0, 11)
    } yield num

  /**
   * 4.2 Создайте коллукцию из 10 выше описанных эффектов (eff)
   */
  lazy val effects: Iterable[ZIO[Random with Clock, Nothing, Int]] = ZIO.replicate(10)(eff)


  /**
   * 4.3 Напишите программу которая вычислит сумму элементов коллекции "effects",
   * напечатает ее в консоль и вернет результат, а также залогирует затраченное время на выполнение,
   * можно использовать ф-цию printEffectRunningTime, которую мы разработали на занятиях
   */

  lazy val app =
    for {
      start <- currentTime(MILLISECONDS)
      sumOfNums <- ZIO.mergeAll(effects)(0) { _ + _ }
      _ <- putStrLn(s"The sum is $sumOfNums")
      end <- currentTime(MILLISECONDS)
      _ <- putStrLn(s"The effect execution took: ${ end - start } millis.")
    } yield sumOfNums // де-факто это будет одно и то же число, потому что 10 раз вызываем генератор числе в одном и том же состоянии с одинаковым сидом


  /**
   * 4.4 Усовершенствуйте программу 4.3 так, чтобы минимизировать время ее выполнения
   */

  lazy val appSpeedUp =
    for {
      start <- currentTime(MILLISECONDS)
      sumOfNums <- ZIO.mergeAllPar(effects)(0) { _ + _ } // Параллельно
      _ <- putStrLn(s"The sum is $sumOfNums")
      end <- currentTime(MILLISECONDS)
      _ <- putStrLn(s"The effect execution took: ${ end - start } millis.")
    } yield sumOfNums


  /**
   * 5. Оформите ф-цию printEffectRunningTime разработанную на занятиях в отдельный сервис, так чтобы ее
   * можно было использовать аналогично zio.console.putStrLn например
   */

  def printEffectRunningTime[D, E, O](effectToTrack: ZIO[D, E, O]): ZIO[Console with Clock with D, E, O] =
    for {
      start <- currentTime(MILLISECONDS)
      value <- effectToTrack
      end <- currentTime(MILLISECONDS)
      _ <- putStrLn(s"The effect execution took: ${ end - start } millis.")
    } yield value

  /**
   * 6.
   * Воспользуйтесь написанным сервисом, чтобы созадть эффект, который будет логировать время выполнения прогаммы из пункта 4.3
   *
   *
   */

  lazy val appWithTimeLogg = printEffectRunningTime(app) // Странно печатать время дважды, но согласно заданию

  /**
   *
   * Подготовьте его к запуску и затем запустите воспользовавшись ZioHomeWorkApp
   */

  lazy val runApp: ZIO[Any, Any, Int] = appWithTimeLogg.provideLayer(Console.live ++ Random.live ++ Clock.live)

}
