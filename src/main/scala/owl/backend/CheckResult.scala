package owl.backend

case class CheckResult(answer: AnyTypeAnswer, grade: Int)

object CheckResult {
  def apply(answer: AnyTypeAnswer, boolGrade: Boolean): CheckResult = CheckResult(answer, if (boolGrade) 100 else 0)
}
