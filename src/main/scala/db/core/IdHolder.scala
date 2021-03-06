package db.core

import java.util.concurrent.atomic.AtomicLong

import scalikejdbc._

trait IdHolder { self: TableObject[_] =>

  private final val emptyId: Long = -1

  private lazy val id = getFromDB map { k =>
    new AtomicLong(k)
  } getOrElse {
    new AtomicLong(emptyId)
  }

  private def getFromDB = DB readOnly { implicit session =>
    sql"SELECT COUNT(*) FROM ${self.sqlName}"
      .map(rs => rs.long(1))
      .single()
      .apply()
  }

  protected def nextId: Option[Long] = {
    if (id.get() == emptyId) {
      val next = getFromDB map (_ + 1)
      next.foreach(k => id.set(k))
      next
    } else {
      Some(id.getAndIncrement() + 1)
    }
  }

}
