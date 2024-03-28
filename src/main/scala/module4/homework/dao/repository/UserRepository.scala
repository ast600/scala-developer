package module4.homework.dao.repository

import io.getquill.ast.BooleanOperator.&&
import io.getquill.context.ZioJdbc._
import io.getquill.context.qzio.ZioJdbcContext
import io.getquill.{ H2Dialect, Literal }
import module4.homework.dao.entity.{ Role, RoleCode, User, UserId, UserToRole }
import zio.{ Has, ZIO, ZLayer }


object UserRepository {


  type UserRepository = Has[Service]

  trait Service {
    def findUser(userId: UserId): QIO[Option[User]]

    def createUser(user: User): QIO[User]

    def createUsers(users: List[User]): QIO[List[User]]

    def updateUser(user: User): QIO[Unit]

    def addRole(role: Role): QIO[Unit]

    def deleteUser(user: User): QIO[Unit]

    def findByLastName(lastName: String): QIO[List[User]]

    def list(): QIO[List[User]]

    def userRoles(userId: UserId): QIO[List[Role]]

    def insertRoleToUser(roleCode: RoleCode, userId: UserId): QIO[Unit]

    def listUsersWithRole(roleCode: RoleCode): QIO[List[User]]

    def findRoleByCode(roleCode: RoleCode): QIO[Option[Role]]
  }

  class ServiceImpl(val ctx: ZioJdbcContext[H2Dialect, Literal]) extends Service {

    import ctx._


    lazy val userSchema = quote {
      querySchema[User]("User")
    }


    lazy val roleSchema = quote {
      querySchema[Role]("Role")
    }

    lazy val userToRoleSchema = quote {
      querySchema[UserToRole]("UserToRole")
    }

    def findUser(userId: UserId): Result[Option[User]] = run(userSchema.filter(_.id == lift(userId.id)))
      .map(_.headOption)

    def createUser(user: User): Result[User] = run(userSchema.insert(lift(user))).as(user)

    def createUsers(users: List[User]): Result[List[User]] =
      run(quote {
        liftQuery(users).foreach { u => userSchema.insert(u) }
      }).map { _ => users }

    def updateUser(user: User): Result[Unit] =
      run(quote { userSchema.filter { _.id == lift(user.id) }.update(lift(user)) }).unit

    def deleteUser(user: User): Result[Unit] =
      run(userSchema.filter { _.id == lift(user.id) }.delete).unit

    def findByLastName(lastName: String): Result[List[User]] =
      run(userSchema.filter { _.lastName == lift(lastName) })

    def list(): Result[List[User]] = run(userSchema)

    def userRoles(userId: UserId): Result[List[Role]] = {
      val query = quote {
        for {
          user <- userSchema.filter { _.id == lift(userId.id) }
          link <- userToRoleSchema if user.id == link.userId
          role <- roleSchema if link.roleId == role.code
        } yield role
      }

      run(query)
    }

    def addRole(role: Role): Result[Unit] =
      ctx.transaction(
        for {
          existsFlg <- run(roleSchema.filter(_.code == lift(role.code))).map { _.nonEmpty }
          _ <- if (existsFlg) ZIO.unit else run(roleSchema.insert(_.code -> lift(role.code), _.name -> lift(role.name)))
        } yield ()
        ).orDie // транзакция

    def insertRoleToUser(roleCode: RoleCode, userId: UserId): Result[Unit] =
      run(userToRoleSchema.insert(_.roleId -> lift(roleCode.code), _.userId -> lift(userId.id))).unit


    def listUsersWithRole(roleCode: RoleCode): Result[List[User]] = {
      val query = quote {
        for {
          users <- userSchema
          link <- userToRoleSchema.join(l => l.userId == users.id)
          role <- roleSchema.filter { _.code == lift(roleCode.code) }.join(r => r.code == link.roleId)
        } yield (users, link, role)._1
      }

      run(query.distinct)
    }

    def findRoleByCode(roleCode: RoleCode): Result[Option[Role]] =
      run(roleSchema.filter { _.code == lift(roleCode.code) }).map { _.headOption }

  }

  val live: ZLayer[Has[ZioJdbcContext[H2Dialect, Literal]], Nothing, Has[Service]] =
    ZLayer.fromService[ZioJdbcContext[H2Dialect, Literal], UserRepository.Service] { ctx => new ServiceImpl(ctx) }
}