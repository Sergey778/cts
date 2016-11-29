package db

import scalikejdbc._

import scala.util.Random

case class Test(id: BigInt, name: String, creator: User) {

  def concreteQuestions: List[Question] = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"SELECT question_id, test_id FROM test_question"
        .map(x => Question.findById(x.bigInt("question_id")))
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
        .map(x => (QuestionGroup.findById(x.bigInt("question_group_id")), x.int("question_count")))
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

object Test {

  def nextId = using(DB(ConnectionPool.borrow())) { db =>
    db localTx { implicit session =>
      sql"SELECT nextval('test_seq')"
        .map(x => x.bigInt(1))
        .single()
        .apply()
    }
  }

  def create(name: String, creator: User) = nextId flatMap { id =>
    val result = using(DB(ConnectionPool.borrow())) { db =>
      db localTx { implicit session =>
        sql"""
            INSERT INTO test (test_id, test_name, test_creator_id)
              VALUES (${BigInt(id)}, $name, ${creator.id})
         """
          .update()
          .apply()
      }
    }
    if (result > 0) Some(Test(BigInt(id), name, creator))
    else None
  }

  def fromResultSet(rs: WrappedResultSet) = Test(
    rs.bigInt("test_id"),
    rs.string("test_name"),
    User.findById(rs.bigInt("test_creator_id")).get
  )

  def fromId(id: BigInt) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"SELECT test_id, test_name, test_creator_id FROM test WHERE test_id = $id"
        .map(x => fromResultSet(x))
        .single()
        .apply()
    }
  }

  def fromCreator(user: User) = using(DB(ConnectionPool.borrow())) { db =>
    db readOnly { implicit session =>
      sql"""
          SELECT test_id, test_name, test_creator_id FROM test
          WHERE test_creator_id = ${user.id}
        """
        .map(rs => Test(rs.bigInt("test_id"), rs.string("test_name"), user))
        .list()
        .apply()
    }
  }
}
