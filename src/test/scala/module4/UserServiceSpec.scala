package module4

import io.getquill.context.ZioJdbc.DataSourceLayer
import module4.homework.dao.entity.{ Role, RoleCode, User }
import module4.homework.dao.repository.UserRepository
import module4.homework.services.UserService
import module4.services.H2
import module4.services.abst.Migration
import module4.services.impl.LiquibaseMigration
import zio.ZIO
import zio.test.Assertion._
import zio.test._

import java.util.UUID


object UserServiceSpec extends DefaultRunnableSpec {

  val MyDataSourceLayer = DataSourceLayer.fromPrefix("myH2DB")
  val MigrationLayer = MyDataSourceLayer >>> LiquibaseMigration.live
  val UserRepositoryLayer = H2.live >>> UserRepository.live
  val UserServiceLayer = UserRepositoryLayer >>> UserService.live

  val SpecLayer = MyDataSourceLayer ++ MigrationLayer ++ UserRepositoryLayer ++ UserServiceLayer

  val users = List(
    User(UUID.randomUUID().toString(), scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString(), scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString(), scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString(), scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString(), scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString(), scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString(), scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString(), scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString(), scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120)),
    User(UUID.randomUUID().toString(), scala.util.Random.nextString(15), scala.util.Random.nextString(30),
         scala.util.Random.nextInt(120))
    )

  val Manager = RoleCode("manager")
  val ManagerRole = Role(Manager.code, "Manager")

  def spec = MySuite.provideCustomLayer(SpecLayer.orDie)

  val MySuite = suite("UserServiceSpec")(
    testM("list user with role Manager should return empty List")(
      for {
        userRepo <- ZIO.environment[UserRepository.UserRepository].map(_.get)
        userService <- ZIO.environment[UserService.UserService].map(_.get)
        _ <- userRepo.createUsers(users)
        result <- userService.listUsersWithRole(Manager)
      } yield assert(result)(isEmpty)
      ),
    testM("add user with role")(
      for {
        userRepo <- ZIO.environment[UserRepository.UserRepository].map(_.get)
        userService <- ZIO.environment[UserService.UserService].map(_.get)
        _ <- userRepo.addRole(ManagerRole)
        _ <- userService.addUserWithRole(users.head, Manager)
        result <- userService.listUsersWithRole(Manager)
      } yield assert(result.length)(equalTo(1)) &&
        assert(result.head.user)(equalTo(users.head)) && assert(result.head.roles)(
        equalTo(Set(ManagerRole)))
      ),
    testM("list user with role Manager should return one Entry")(
      for {
        userService <- ZIO.environment[UserService.UserService].map(_.get)
        result <- userService.listUsersWithRole(Manager)
      } yield assert(result.length)(equalTo(1)) && assert(result.head.user)(equalTo(users.head)) &&
        assert(result.head.roles)(equalTo(Set(Role(Manager.code, "Manager"))))
      )
    ) @@ TestAspect.beforeAll(Migration.initializeZtructure.useNow) @@ TestAspect.sequential
}
