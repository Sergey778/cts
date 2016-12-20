package db

import db.core.{IdHolder, Table, TableObject}
import scalikejdbc._

case class QuestionGroup(
                          id: BigInt,
                          name: String,
                          creator: User,
                          private val parentGroupEval: () => Option[QuestionGroup]) extends Table {

  lazy val parentGroup: Option[QuestionGroup] = parentGroupEval()

  def childs: List[QuestionGroup] = QuestionGroup.withParent(id)

  def questions: List[Question] = Question.withQuestionGroup(this)

  def questionsCount = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"SELECT COUNT(question_id) FROM question WHERE question_group_id = $id"
        .map(x => x.int(1))
        .single()
        .apply()
    }
  }
}

object QuestionGroup extends TableObject[QuestionGroup] with IdHolder {

  override def name: String = "question_group"

  private final val $id = "id"
  private final val $name = "name"
  private final val $creator = "creator"
  private final val $parentGroup = "parentGroup"

  override def columns: Map[String, String] = Map (
    $id -> "question_group_id",
    $name -> "question_group_name",
    $creator -> "question_group_creator_id",
    $parentGroup -> "question_group_parent_id"
  )

  def create(name: String, creator: User, parentGroup: Option[QuestionGroup]): Option[QuestionGroup] = nextId flatMap { id =>
    val result = using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        insertSql(
          $id -> BigInt(id),
          $name -> name,
          $creator -> creator.id,
          $parentGroup -> parentGroup.map(x => x.id)
        )
        .update()
        .apply()
      }
    }
    if (result > 0) Some(QuestionGroup(BigInt(id), name, creator, () => parentGroup)) else None
  }

  override def fromResultSet(rs: WrappedResultSet): QuestionGroup = fromResultSet(rs, None, None, None)

  def fromResultSet(rs: WrappedResultSet,
                    id: Option[BigInt] = None,
                    creator: Option[User] = None,
                    name: Option[String] = None,
                    parentGroup: Option[Option[QuestionGroup]] = None): QuestionGroup = QuestionGroup(
    id = id.getOrElse(rs.bigInt(columns($id))),
    name = name.getOrElse(rs.string(columns($name))),
    creator = creator.orElse(User.findById(rs.bigInt(columns($creator)))).get,
    parentGroupEval = () => parentGroup.getOrElse(QuestionGroup.withId(rs.bigInt(columns($parentGroup))))
  )

  def withCreator(creator: User): List[QuestionGroup] = {
    using(DB(ConnectionPool.borrow())) { db =>
      db readOnly { implicit session =>
        sql"""
            SELECT question_group_id, question_group_name, question_group_creator_id, question_group_parent_id
            FROM question_group
            WHERE question_group_creator_id = ${creator.id} AND question_group_parent_id IS NULL
          """
          .map(rs => fromResultSet(rs))
          .list()
          .apply()
      }
    }
  }

  def withId(id: BigInt): Option[QuestionGroup] = whereOption($id -> id)

  def withParent(id: BigInt): List[QuestionGroup] = whereList($parentGroup -> id)
  def withParent(parentGroup: QuestionGroup): List[QuestionGroup] = DB readOnly { implicit session =>
    whereSql($parentGroup -> parentGroup.id)
      .map(rs => fromResultSet(rs, parentGroup = Some(Some(parentGroup))))
      .list()
      .apply()
  }
}
