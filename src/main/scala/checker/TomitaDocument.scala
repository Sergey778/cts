package checker

import java.time.LocalDateTime

case class TomitaDocument(bi: String, di: String, date: LocalDateTime, facts: Seq[TomitaFact]) {
  def compare(document: TomitaDocument): Int = {
    val subjects = this.subjects
    val objects = this.objects
    val relations = this.relations
    val all = List(subjects.size, objects.size, relations.size).map(_ > 0).map {
      case true => 1
      case _ => 0
    }
    val subjectSize = if (subjects.nonEmpty) document.subjects.toSet.size.toDouble / subjects.size else 0.0
    val objectSize = if (objects.nonEmpty) document.objects.toSet.size.toDouble / objects.size else 0.0
    val relationSize = if (relations.nonEmpty) document.relations.toSet.size.toDouble / relations.size else 0.0

    val result = (subjectSize + objectSize + relationSize) / all.sum * 100
    result.toInt
  }

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
