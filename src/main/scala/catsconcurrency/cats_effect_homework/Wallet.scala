package catsconcurrency.cats_effect_homework

import cats.effect.Sync
import cats.implicits._
import catsconcurrency.cats_effect_homework.Wallet._

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Path, Paths, StandardOpenOption }

// DSL управления электронным кошельком
trait Wallet[F[_]] {
  // возвращает текущий баланс
  def balance: F[BigDecimal]

  // пополняет баланс на указанную сумму
  def topup(amount: BigDecimal): F[Unit]

  // списывает указанную сумму с баланса (ошибка если средств недостаточно)
  def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]]
}

// Игрушечный кошелек который сохраняет свой баланс в файл
// todo: реализовать используя java.nio.file._
// Насчёт безопасного конкуррентного доступа и производительности не заморачиваемся, делаем максимально простую рабочую имплементацию. (Подсказка - можно читать и сохранять файл на каждую операцию).
// Важно аккуратно и правильно завернуть в IO все возможные побочные эффекты.
//
// функции которые пригодятся:
// - java.nio.file.Files.write
// - java.nio.file.Files.readString
// - java.nio.file.Files.exists
// - java.nio.file.Paths.get
final class FileWallet[F[_] : Sync](id: WalletId) extends Wallet[F] {

  import FileWallet.TmpDirectoryPath

  def balance: F[BigDecimal] = Sync[F]
    .delay { Files.readString(TmpDirectoryPath.resolve(s"$id.txt"), StandardCharsets.UTF_8) }
    .map { decimal => BigDecimal(decimal) }


  def topup(amount: BigDecimal): F[Unit] = {
    balance.map { _ + amount }
           .flatMap { writeBalance }
  }

  def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]] =
    balance.flatMap {
      case sum if sum >= amount => writeBalance(sum - amount).map { _.asRight[WalletError] }
      case _ => BalanceTooLow.pure[F].map { _.asLeft[Unit] }
    }

  private def writeBalance(b: BigDecimal): F[Unit] = {
    Sync[F].delay {
      Files.write(TmpDirectoryPath.resolve(s"$id.txt"),
                  b.toString().getBytes(StandardCharsets.UTF_8),
                  StandardOpenOption.TRUNCATE_EXISTING)
    }
  }
}

object FileWallet {
  // Можно было бы обернуть в IO/Sync из-за side-effect'ов со стороны java.net.URI
  // Возможно, при запуске на Windows придется поиграть со схемой (писал на Ubuntu)
  val TmpDirectoryPath: Path = Paths.get(URI.create("file://" + System.getProperty("java.io.tmpdir") + "/"))
}

object Wallet {

  // todo: реализовать конструктор
  // внимание на сигнатуру результата - инициализация кошелька имеет сайд-эффекты
  // Здесь нужно использовать обобщенную версию уже пройденного вами метода IO.delay,
  // вызывается она так: Sync[F].delay(...)
  // Тайпкласс Sync из cats-effect описывает возможность заворачивания сайд-эффектов
  def fileWallet[F[_] : Sync](id: WalletId): F[Wallet[F]] = {
    val underlyingFilePath = FileWallet.TmpDirectoryPath.resolve(s"$id.txt")

    val createFileIfNotExistsEff = for {
      fileNotExists <- Sync[F].delay { Files.notExists(underlyingFilePath) }
      _ <- if (fileNotExists) Sync[F].delay {
        Files.write(underlyingFilePath,
                    BigDecimal(0).toString.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE_NEW)
      } else Sync[F].unit
    } yield ()

    createFileIfNotExistsEff >> Sync[F].pure { new FileWallet[F](id) }
  }

  type WalletId = String

  sealed trait WalletError

  case object BalanceTooLow extends WalletError
}
