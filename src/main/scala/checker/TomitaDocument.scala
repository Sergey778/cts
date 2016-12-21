package checker

import java.time.LocalDateTime

case class TomitaDocument(bi: String, di: String, date: LocalDateTime, facts: Seq[TomitaFact]) {
  def subjects: Seq[TomitaFact] = facts filter {
    case SubjectFact(_) => true
    case _ => false
  }

  def objects: Seq[TomitaFact] = facts filter {
    case ObjectFact(_) => true
    case _ => false
  }

  def relations: Seq[TomitaFact] = facts filter {
    case RelationFact(_) => true
    case _ => false
  }
}
