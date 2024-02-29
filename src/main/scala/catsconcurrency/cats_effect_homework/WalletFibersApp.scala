package catsconcurrency.cats_effect_homework

import cats.Monad
import cats.effect.{ IO, IOApp }
import cats.implicits._

import scala.concurrent.duration.DurationInt

// Поиграемся с кошельками на файлах и файберами.

// Нужно написать программу где инициализируются три разных кошелька и для каждого из них работает фоновый процесс,
// который регулярно пополняет кошелек на 100 рублей раз в определенный промежуток времени. Промежуток надо сделать разный, чтобы легче было наблюдать разницу.
// Для определенности: первый кошелек пополняем раз в 100ms, второй каждые 500ms и третий каждые 2000ms.
// Помимо этих трёх фоновых процессов (подсказка - это файберы), нужен четвертый, который раз в одну секунду будет выводить балансы всех трех кошельков в консоль.
// Основной процесс программы должен просто ждать ввода пользователя (IO.readline) и завершить программу (включая все фоновые процессы) когда ввод будет получен.
// Итого у нас 5 процессов: 3 фоновых процесса регулярного пополнения кошельков, 1 фоновый процесс регулярного вывода балансов на экран и 1 основной процесс просто ждущий ввода пользователя.

// Можно делать всё на IO, tagless final тут не нужен.

// Подсказка: чтобы сделать бесконечный цикл на IO достаточно сделать рекурсивный вызов через flatMap:
// def loop(): IO[Unit] = IO.println("hello").flatMap(_ => loop())
object WalletFibersApp extends IOApp.Simple {

  def run: IO[Unit] =
    for {
      _ <- IO.println("Press Enter to stop...")
      wallet1 <- Wallet.fileWallet[IO]("1")
      wallet2 <- Wallet.fileWallet[IO]("2")
      wallet3 <- Wallet.fileWallet[IO]("3")
      fib1 <- Monad[IO].whileM_(IO(true)) { IO.sleep(100.millis) >> wallet1.topup(BigDecimal(100)) }.start
      fib2 <- Monad[IO].whileM_(IO(true)) { IO.sleep(500.millis) >> wallet2.topup(BigDecimal(100)) }.start
      fib3 <- Monad[IO].whileM_(IO(true)) { IO.sleep(2000.millis) >> wallet3.topup(BigDecimal(100)) }.start
      fibSupervisor <- Monad[IO].whileM_(IO(true)) {
        IO.sleep(1.seconds) >> wallet1.balance.flatMap(b => IO.println(s"The wallet1 balance is $b")) >>
          wallet2.balance.flatMap(b => IO.println(s"The wallet2 balance is $b")) >>
          wallet3.balance.flatMap(b => IO.println(s"The wallet3 balance is $b"))
      }.start
      _ <- IO.readLine
      _ <- fib1.cancel
      _ <- fib2.cancel
      _ <- fib3.cancel
      _ <- fibSupervisor.cancel
      // todo: запустить все файберы и ждать ввода от пользователя чтобы завершить работу
    } yield ()

}
