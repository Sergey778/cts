package db.core

import scalikejdbc._

trait TableObject[A <: Table] {
  def name: String

  protected val sqlName: SQLSyntax = SQLSyntax.createUnsafely(name)

  def columns: Map[String, String]

  def columnNames: Seq[String] = columns.values.toSeq

  def fromResultSet(rs: WrappedResultSet): A

  protected val sqlAll: SQLSyntax = SQLSyntax.createUnsafely(columnNames.mkString(", "))

  protected def insertSql(values: (String, Any)*): SQL[Nothing, NoExtractor] = {
    val argNames = SQLSyntax.createUnsafely(s"${values.map(x => columns(x._1)).mkString(", ")}")
    sql"INSERT INTO $sqlName ($argNames) VALUES (${values.map(x => sqls"${x._2}")})"
  }


  def all: List[A] = DB readOnly { implicit session =>
    sql"SELECT $sqlAll FROM $sqlName"
      .map(rs => fromResultSet(rs))
      .list()
      .apply()
  }

  protected def whereSql[ValueType](columnName: String, value: ValueType): SQL[Nothing, NoExtractor] =
    sql"SELECT $sqlAll FROM $sqlName WHERE ${SQLSyntax.createUnsafely(columns(columnName))} = $value"

  protected def whereSql[ValueType](column: (String, ValueType)): SQL[Nothing, NoExtractor] =
    whereSql(column._1, column._2)

  def whereList[ValueType](columnName: String, value: ValueType): List[A] = DB readOnly { implicit session =>
    whereSql(columnName -> value)
      .map(rs => fromResultSet(rs))
      .list()
      .apply()
  }

  def whereList[ValueType](column: (String, ValueType)): List[A] = whereList(column._1, column._2)

  def whereOption[ValueType](columnName: String, value: ValueType): Option[A] = whereList(columnName, value).headOption

  def whereOption[ValueType](column: (String, ValueType)): Option[A] = whereList(column).headOption
}
