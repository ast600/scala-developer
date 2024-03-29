package module4.services

import io.getquill.context.qzio.ZioJdbcContext
import io.getquill.{ Literal, H2Dialect, H2ZioJdbcContext }
import zio.{ Has, ZIO, ZLayer, ZManaged }

object H2 {
  val live: ZLayer[Any, Throwable, Has[ZioJdbcContext[H2Dialect, Literal]]] = ZLayer.fromManaged {
    ZManaged.make{
      for {
        ctx <- ZIO.effect { new H2ZioJdbcContext(Literal) }
      } yield ctx.asInstanceOf[ZioJdbcContext[H2Dialect, Literal]]
    } {
      ctx => ZIO.effect { ctx.close() }.orDie
    }
  }
}
