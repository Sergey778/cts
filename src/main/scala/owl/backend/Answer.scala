package owl.backend

sealed trait AnyTypeAnswer {
  def question: Question
}

sealed trait Answer[A] extends AnyTypeAnswer {
  def value: A
}

trait TextAnswer extends Answer[String] { extractor: WordExtractor[String] =>
  def words: Seq[Word] = extractor.extract(value)
}
trait NumberAnswer extends Answer[Int]
trait ListAnswer[A <: Answer[_]] extends Answer[Seq[_]]

