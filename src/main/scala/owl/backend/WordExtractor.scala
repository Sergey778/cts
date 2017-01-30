package owl.backend

trait WordExtractor[A] {
  def extract(expression: A): Seq[Word]
}
