package owl.backend

trait Checker[A <: Answer[_]] {
  def check(standard: A, answer: A): CheckResult

  def answerLengthRange: Range

  def priority: Short
}
