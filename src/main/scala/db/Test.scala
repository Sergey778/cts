package db

import scalikejdbc._

case class Test(id: BigInt, name: String, creator: User)

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
              VALUES (${BigInt(id)}, ${name}, ${creator.id})
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
      sql"SELECT test_id, test_name, test_creator_id FROM test WHERE test_id = ${id}"
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
