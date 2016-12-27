package db

import db.core.{IdHolder, Table, TableObject}
import scalikejdbc._

case class Word(id: BigInt, value: String) extends Table {

  lazy val synonyms: List[Word] = DB readOnly { implicit session =>
    sql"""
       SELECT
         w.word_id, w.word_value
       FROM
         words w
       WHERE w.word_id IN (
       SELECT word_id FROM word_synonyms WHERE synonym_id = ${id}
       UNION ALL
       SELECT synonym_id FROM word_synonyms WHERE word_id = ${id}
       );
       """
      .map(rs => Word.fromResultSet(rs))
      .list()
      .apply()
  }

}

object Word extends TableObject[Word] with IdHolder {
  override def name: String = "words"

  private final val $id = "id"
  private final val $value = "value"

  override def columns: Map[String, String] = Map (
    $id -> "word_id",
    $value -> "word_value"
  )

  def fromResultSet(rs: WrappedResultSet,
                    id: Option[BigInt] = None,
                    value: Option[String] = None): Word = Word(
    id = id.getOrElse(rs.bigInt(columns($id))),
    value = value.getOrElse(rs.string(columns($value)))
  )

  override def fromResultSet(rs: WrappedResultSet): Word = fromResultSet(rs, None, None)

  def withValue(value: String): Option[Word] = whereOption($value -> value)
  def withId(id: BigInt): Option[Word] = whereOption($id -> id)

  def apply(value: String): Word = withValue(value).getOrElse(Word(-1, value))
}
