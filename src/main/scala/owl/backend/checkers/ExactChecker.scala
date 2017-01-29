package owl.backend.checkers

import owl.backend._

object ExactChecker extends Checker[Answer[_]] {
  override def check(standard: Answer[_], answer: Answer[_]): CheckResult =
    CheckResult(answer, standard.value == answer.value)

  override def answerLengthRange: Range = 0 to Int.MaxValue

  override def priority: Short = 100
}
