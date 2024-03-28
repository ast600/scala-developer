package module4.services.impl

import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import module4.services.abst.Migration
import zio.{ ZIO, ZLayer, ZManaged }

import javax.sql.DataSource

class LiquibaseMigration(ds: DataSource) extends Migration.Service {

  override def initializeStructure: ZManaged[Any, Throwable, Unit] =
    for {
      source <- ZIO.succeed(ds).toManaged_
      classLoader = this.getClass.getClassLoader
      accessor = new ClassLoaderResourceAccessor(classLoader)
      conn <- ZManaged.makeEffect{ new JdbcConnection(source.getConnection) } { _.close() }
      service <- ZIO.effect { new Liquibase("liquibase/init.xml", accessor, conn) }.toManaged_
      _ <- ZIO.effect { service.update("test") }.toManaged_
    } yield ()

}

object LiquibaseMigration {
  val live = ZLayer.fromService[DataSource, Migration.Service] { ds => new LiquibaseMigration(ds) }
}
