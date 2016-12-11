package db.core

import java.sql.Timestamp
import java.time.LocalDateTime

trait TimeHolder { self: TableObject[_] =>
  def currentTime: LocalDateTime = LocalDateTime.now()
  def currentTimestamp: Timestamp = Timestamp.valueOf(currentTime)
}
