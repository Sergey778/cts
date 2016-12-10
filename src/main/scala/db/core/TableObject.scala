package db.core

import scalikejdbc._

trait TableObject[A <: TableObject[A]] {
  def name: String

  protected val sqlName: SQLSyntax = sqls"$name"

  def columnsNames: Array[String]

  def fromResultSet(rs: WrappedResultSet): Table
}
