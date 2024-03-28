package module4.services.abst

import zio.{ Has, ZManaged }


object Migration {
  type MigrationService = Has[Service]

  abstract class Service {
    def initializeStructure: ZManaged[Any, Throwable, Unit]
  }

  def initializeZtructure: ZManaged[MigrationService, Throwable, Unit] =
    ZManaged.accessManaged[MigrationService] { _.get.initializeStructure }
}
