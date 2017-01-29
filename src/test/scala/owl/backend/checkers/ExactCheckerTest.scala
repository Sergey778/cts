package owl.backend.checkers

import org.scalatest.{FlatSpec, Matchers}
import owl.backend._

class ExactCheckerTest extends FlatSpec with Matchers {
  "'check' method" must "check if two answers exactly match" in {
    val q = new Question {
      override def value: String = "test?"
    }
    val a = new TextAnswer {
      override def value: String = "yes"

      override def question: Question = q
    }

    val b = new TextAnswer {
      override def value: String = "yes"

      override def question: Question = q
    }

    val c = a

    val d = new TextAnswer {
      override def value: String = "no"

      override def question: Question = q
    }

    ExactChecker.check(a, b) shouldEqual CheckResult(b, 100)
    ExactChecker.check(a, c) shouldEqual CheckResult(a, 100)
    ExactChecker.check(a, d) shouldEqual CheckResult(d, 0)
  }

  it must "compare any answer type" in {
    val q = new Question {
      override def value = "how much?"
    }

    val a = new NumberAnswer {
      override def value = 100

      override def question = q
    }

    val b = new NumberAnswer {
      override def value = 100

      override def question = q
    }

    val c =new NumberAnswer {
      override def value = 99

      override def question = q
    }

    ExactChecker.check(a, b) shouldEqual CheckResult(b, 100)
    ExactChecker.check(a, c) shouldEqual CheckResult(c, 0)
  }
}
