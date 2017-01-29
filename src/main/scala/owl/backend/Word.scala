package owl.backend

trait Word {
  val value: String
  def synonyms: Seq[Word]

  override def equals(other: Any): Boolean = other match {
    case Word(v) if value == v => true
    case _ => false
  }
}

object Word {
  def unapply(word: Word): Option[String] = Some(word.value)
}