package module4.homework.services

import module4.homework.dao.entity.{ Role, RoleCode, User, UserId }
import module4.homework.dao.repository.UserRepository
import zio.macros.accessible
import zio.{ Has, RIO, ZIO, ZLayer }

import javax.sql.DataSource

@accessible
object UserService {
  type UserService = Has[Service]

  trait Service {
    def listUsers(): RIO[Has[DataSource], List[User]]

    def listUsersDTO(): RIO[Has[DataSource], List[UserDTO]]

    def addUserWithRole(user: User, roleCode: RoleCode): RIO[Has[DataSource], UserDTO]

    def listUsersWithRole(roleCode: RoleCode): RIO[Has[DataSource], List[UserDTO]]
  }

  class Impl(userRepo: UserRepository.Service) extends Service {

    def listUsers(): RIO[Has[DataSource], List[User]] =
      userRepo.list()


    def listUsersDTO(): RIO[Has[DataSource], List[UserDTO]] =
      for {
        users <- listUsers()
        dtos <- ZIO.foreach(users) { user2UserDTO }
      } yield dtos

    def addUserWithRole(user: User, roleCode: RoleCode): RIO[Has[DataSource], UserDTO] =
      for {
        user <- userRepo.createUser(user)
        _ <- userRepo.addRole(Role(roleCode.code, "Manager"))
        _ <- userRepo.insertRoleToUser(roleCode, UserId(user.id))
        dto <- user2UserDTO(user)
      } yield dto

    def listUsersWithRole(roleCode: RoleCode): RIO[Has[DataSource], List[UserDTO]] =
      for {
        users <- userRepo.listUsersWithRole(roleCode)
        dtos <- ZIO.foreach(users) { user2UserDTO }
      } yield dtos


    private def user2UserDTO(u: User): RIO[Has[DataSource], UserDTO] =
      userRepo.userRoles(UserId(u.id)).map { roles => UserDTO(u, roles.toSet) }

  }

  val live: ZLayer[UserRepository.UserRepository, Nothing, UserService] =
    ZLayer.fromService[UserRepository.Service, Service] {
      repo => new Impl(repo)
    }
}

case class UserDTO(user: User, roles: Set[Role])