package module4

import io.getquill.context.ZioJdbc.DataSourceLayer
import module4.homework.dao.entity.{ User, UserId }
import module4.homework.dao.repository.UserRepository
import module4.services.H2
import module4.services.abst.Migration
import module4.services.impl.LiquibaseMigration
import zio.ZIO
import zio.random.Random
import zio.test.Assertion._
import zio.test.{ DefaultRunnableSpec, Gen, Sized, TestAspect, assert, checkAllM }

import java.util.UUID


object UserRepositorySpec extends DefaultRunnableSpec {

  val SpecLayer = H2.live >>> UserRepository.live ++ DataSourceLayer.fromPrefix("myH2DB") >+> LiquibaseMigration.live

  val genName: Gen[Random with Sized, String] = Gen.anyASCIIString
  val genAge: Gen[Random, Int] = Gen.int(18, 120)
  val genUuid: Gen[Random, UUID] = Gen.anyUUID

  val genUser = for {
    uuid <- genUuid
    firstName <- genName
    lastName <- genName
    age <- genAge
  } yield User(uuid.toString, firstName, lastName, age)


  val users = List(
    User(UUID.randomUUID().toString, scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString, scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString, scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString, scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString, scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString, scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString, scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString, scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString, scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString, scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120))
    )
  val usersGen =
    for {
      id <- Gen.anyUUID.map { _.toString }
      firstName <- Gen.stringBounded(5, 15)(Gen.alphaChar)
      lastName <- Gen.stringBounded(5, 30)(Gen.alphaChar)
      age <- Gen.int(18, 120)
    } yield User(id, firstName, lastName, age)


  override def spec = MySuite.provideCustomLayer(SpecLayer.orDie)

  val MySuite = suite("UserRepository suite")(
    testM("метод list возвращает пустую коллекцию, на пустой базе")(
      for {
        userRepo <- ZIO.environment[UserRepository.UserRepository].map(_.get)
        result <- userRepo.list()
      } yield assert(result.isEmpty)(isTrue)
      ),
    testM("методы create а затем findBy по созданному пользователю")(
      checkAllM(usersGen) { user =>
        for {
          userRepo <- ZIO.environment[UserRepository.UserRepository].map(_.get)
          user <- userRepo.createUser(user)
          result <- userRepo.findUser(user.typedId).some.mapError(_ => new Exception("fetch failed"))
        } yield assert(user.id)(equalTo(result.id)) &&
          assert(result.firstName)(equalTo(user.firstName))
      }

      ),
    testM("метод findBy по случайному id")(
      checkAllM(usersGen, Gen.anyUUID) { (user, id) =>
        for {
          userRepo <- ZIO.environment[UserRepository.UserRepository].map(_.get)
          _ <- userRepo.createUser(user)
          result <- userRepo.findUser(UserId(id.toString))
        } yield assert(result)(isNone)
      }

      ),
    testM("метод update должен обновлять только целевого пользователя")(
      for {
        userRepo <- ZIO.environment[UserRepository.UserRepository].map(_.get)
        users <- userRepo.createUsers(users)
        user = users.head
        newFirstName = "Petr"
        _ <- userRepo.updateUser(user.copy(firstName = newFirstName))
        updated <- userRepo.findUser(user.typedId).some.mapError(_ => new Exception("fetch failed"))
      } yield assert(updated.firstName)(equalTo(newFirstName))
      ),
    testM("метод delete должен удалять только целевого пользователя")(
      for {
        userRepo <- ZIO.environment[UserRepository.UserRepository].map(_.get)
        user = users.last
        _ <- userRepo.deleteUser(user)
        all <- userRepo.list()
      } yield assert(!all.exists(_.id == user.id))(isTrue)
      ),
    testM("метод findByLastName должен находить пользователя")(
      for {
        userRepo <- ZIO.environment[UserRepository.UserRepository].map(_.get)
        user = users(5)
        result <- userRepo.findByLastName(user.lastName)
      } yield assert(result.nonEmpty)(isTrue)
      )
    ) @@ TestAspect.beforeAll(Migration.initializeZtructure.useNow)
}

// 7c038f1d-4e8c-4c8e-a8ba-dd58b49b62af
// 7c038f1d-4e8c-4c8e-a8ba-dd58b49b62af