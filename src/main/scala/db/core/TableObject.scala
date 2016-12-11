package db.core

import scalikejdbc._

trait TableObject[A <: Table] {
  def name: String

  protected val sqlName: SQLSyntax = SQLSyntax.createUnsafely(name)

  def columns: Map[String, String]

  def columnNames: Seq[String] = columns.values.toSeq

  def fromResultSet(rs: WrappedResultSet): A

  protected val sqlAll: SQLSyntax = SQLSyntax.createUnsafely(columnNames.mkString(", "))

  protected val sqlInsert: SQLSyntax = sqls"INSERT INTO $sqlName ($sqlAll)"

  protected def insertN1[T1](v1: T1) =
    sql"$sqlInsert VALUES ($v1)"
  protected def insertN2[T1, T2](v1: T1, v2: T2) =
    sql"$sqlInsert VALUES ($v1, $v2)"
  protected def insertN3[T1, T2, T3](v1: T1, v2: T2, v3: T3) =
    sql"$sqlInsert VALUES ($v1, $v2, $v3)"
  protected def insertN4[T1, T2, T3, T4](v1: T1, v2: T2, v3: T3, v4: T4) =
    sql"$sqlInsert VALUES ($v1, $v2, $v3, $v4)"
  protected def insertN5[T1, T2, T3, T4, T5](v1: T1, v2: T2, v3: T3, v4: T4, v5: T5) =
    sql"$sqlInsert VALUES ($v1, $v2, $v3, $v4, $v5)"
  protected def insertN6[T1, T2, T3, T4, T5, T6](v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6) =
    sql"$sqlInsert VALUES ($v1, $v2, $v3, $v4, $v5, $v6)"
  protected def insertN7[T1, T2, T3, T4, T5, T6, T7](v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7) =
    sql"$sqlInsert VALUES ($v1, $v2, $v3, $v4, $v5, $v6, $v7)"


  def all: List[A] = DB readOnly { implicit session =>
    sql"SELECT $sqlAll FROM $sqlName"
      .map(rs => fromResultSet(rs))
      .list()
      .apply()
  }

  def whereList[ValueType](columnName: String, value: ValueType): List[A] = DB readOnly { implicit session =>
    val columnSQL = SQLSyntax.createUnsafely(columns(columnName))
    sql"SELECT $sqlAll FROM $sqlName WHERE $columnSQL = $value"
      .map(rs => fromResultSet(rs))
      .list()
      .apply()
  }

  def whereList[ValueType](column: (String, ValueType)): List[A] = whereList(column._1, column._2)

  def whereOption[ValueType](columnName: String, value: ValueType): Option[A] = whereList(columnName, value).headOption

  def whereOption[ValueType](column: (String, ValueType)): Option[A] = whereList(column).headOption
}
