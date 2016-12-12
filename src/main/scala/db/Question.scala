package db

import java.time.LocalDateTime

import db.core._
import scalikejdbc._

case class Question (
                      id: BigInt,
                      creator: User,
                      modifier: User,
                      createTime: LocalDateTime,
                      modifyTime: LocalDateTime,
                      text: String,
                      group: QuestionGroup
                    ) extends Table

object Question extends TableObject[Question] with IdHolder with TimeHolder {

  override final def name: String = "question"

  private final val $id = "id"
  private final val $creator = "creator"
  private final val $modifier = "modifier"
  private final val $createTime = "createTime"
  private final val $modifyTime = "modifyTime"
  private final val $text = "text"
  private final val $group = "group"

  override final def columns: Map[String, String] = Map (
    $id -> "question_id",
    $creator -> "question_creator_id",
    $modifier -> "question_modifier_id",
    $createTime -> "question_create_time",
    $modifyTime -> "question_modify_time",
    $text -> "question_text",
    $group -> "question_group_id"
  )

  override def fromResultSet(rs: WrappedResultSet): Question = fromResultSet(rs, None, None, None)

  def withId(id: BigInt): Option[Question] = whereOption($id -> id)

  def fromResultSet(rs: WrappedResultSet,
                    creator: Option[User] = None,
                    modifier: Option[User] = None,
                    group: Option[QuestionGroup] = None): Question = Question(
    id = rs.bigInt("question_id"),
    creator = creator.orElse(User.findById(rs.bigInt("question_creator_id"))).get,
    modifier = modifier.orElse(User.findById(rs.bigInt("question_modifier_id"))).get,
    createTime = rs.timestamp("question_create_time").toLocalDateTime,
    modifyTime = rs.timestamp("question_modify_time").toLocalDateTime,
    text = rs.string("question_text"),
    group = group.orElse(QuestionGroup.findById(rs.bigInt("question_group_id"))).get
  )

  def withCreator(creator: User): List[Question] = DB readOnly { implicit session =>
    whereSql($creator -> creator.id)
      .map(x => fromResultSet(x, creator = Some(creator)))
      .list()
      .apply()
  }

  def create(creator: User, text: String, group: QuestionGroup): Option[Question] = nextId flatMap { id =>
    val time = currentTime
    val result = using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        /*sql"""
           INSERT INTO $sqlName ($sqlAll)
           VALUES ($id, ${creator.id}, ${creator.id}, $timestamp, $timestamp, $text, ${group.id})
         """*/
        insertSql($id -> id,
          $creator -> creator.id,
          $modifier -> creator.id,
          $createTime -> time,
          $modifyTime -> time,
          $text -> text,
          $group -> group.id)
          .update()
          .apply()
      }
    }
    if (result > 0) Some(Question(id, creator, creator, time, time, text, group)) else None
  }
}
