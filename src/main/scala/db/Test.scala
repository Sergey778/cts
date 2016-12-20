package db

import db.core.{IdHolder, Table, TableObject}
import scalikejdbc._

import scala.util.Random

case class Test(id: BigInt, name: String, creator: User) extends Table {

  def concreteQuestions: List[Question] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"SELECT question_id, test_id FROM test_question"
        .map(x => Question.withId(x.bigInt("question_id")))
        .list()
        .apply()
    }
  } filter { question =>
    question.nonEmpty
  } map { question =>
    question.get
  }

  def groupQuestions: List[Question] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"SELECT question_group_id, test_id, question_count FROM test_question_group WHERE test_id = $id"
        .map(x => (QuestionGroup.withId(x.bigInt("question_group_id")), x.int("question_count")))
        .list()
        .apply()
    }
  } flatMap {
    case (Some(group), count) => Random.shuffle(group.questions).takeRight(count)
    case _ => List()
  }

  def addQuestion(question: Question) = using(DB(ConnectionPool.borrow())) { db =>
    db localTx { implicit session =>
      sql"INSERT INTO test_question (test_id, question_id) VALUES ($id, ${question.id})"
        .update()
        .apply()
    }
  }

  def addQuestionGroup(questionGroup: QuestionGroup, count: Int) = questionGroup.questionsCount map { maxCount =>
    val realCount = if (count > maxCount || count <= 0) maxCount else count
    using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql"""
          INSERT INTO test_question_group (test_id, question_group_id, question_count)
              VALUES ($id, ${questionGroup.id}, $realCount)
         """
          .update()
          .apply()
      }
    }
  }

  def questions = groupQuestions ++ concreteQuestions
}

object Test extends TableObject[Test] with IdHolder {


  override def name: String = "test"

  private final val $id = "id"
  private final val $name = "name"
  private final val $creator = "creator"

  override def columns: Map[String, String] = Map(
    $id -> "test_id",
    $name -> "test_name",
    $creator -> "test_creator_id"
  )

  def create(name: String, creator: User): Option[Test] = nextId flatMap { id =>
    val result = DB localTx { implicit session =>
      insertSql(
        $id -> BigInt(id),
        $name -> name,
        $creator -> creator.id
      )
        .update()
        .apply()
    }
    if (result > 0) Some(Test(BigInt(id), name, creator)) else None
  }

  def fromResultSet(rs: WrappedResultSet,
                    id: Option[BigInt] = None,
                    name: Option[String] = None,
                    creator: Option[User] = None): Test = Test(
    id = id.getOrElse(rs.bigInt(columns($id))),
    name = name.getOrElse(rs.string(columns($name))),
    creator = creator.orElse(User.findById(rs.bigInt(columns($creator)))).get
  )

  override def fromResultSet(rs: WrappedResultSet): Test = fromResultSet(rs, None, None, None)

  def withId(id: BigInt): Option[Test] = whereOption($id -> id)

  def withCreator(creator: User): List[Test] = DB readOnly { implicit session =>
    whereSql($creator -> creator.id)
      .map(rs => fromResultSet(rs, creator = Some(creator)))
      .list()
      .apply()
  }
}
