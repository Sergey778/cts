package checker

import java.time.LocalDateTime

import db.Word

case class TomitaDocument(bi: String, di: String, date: LocalDateTime, facts: Seq[TomitaFact]) {
  def compare(document: TomitaDocument): Int = {
    val subjectScore = compareSubjects(document)
    val objectScore = compareObjects(document)
    val relationScore = compareRelations(document)
    (((subjectScore + objectScore + relationScore) / 3.0) * 100).toInt
  }

  private def compareSubjects(document: TomitaDocument) = {
    val thisSubjects = this.subjects.map(x => Word(x.value.value)).toSet
    val thatSubjects = document.subjects.map(x => Word(x.value.value)).toSet
    compareWordSetWithSynonyms(thisSubjects, thatSubjects)
  }

  private def compareObjects(document: TomitaDocument) = {
    val thisObjects = this.objects.map(x => Word(x.value.value)).toSet
    val thatObjects = document.objects.map(x => Word(x.value.value)).toSet
    compareWordSetWithSynonyms(thisObjects, thatObjects)
  }

  private def compareRelations(document: TomitaDocument) = {
    val thisRelations = this.relations.map(x => Word(x.value.value)).toSet
    val thatRelations = document.relations.map(x => Word(x.value.value)).toSet
    compareWordSetWithSynonyms(thisRelations, thatRelations)
  }

  private def compareWordSet(wordSet: Set[Word], otherWordSet: Set[Word]) = {
    if (wordSet.isEmpty) compareEmptyWordSet(wordSet, otherWordSet)
    else {
      val difference = wordSet diff otherWordSet
      1.0 - (difference.size / wordSet.size)
    }
  }

  private def compareEmptyWordSet(wordSet: Set[Word], otherWordSet: Set[Word]) =
    if (otherWordSet.isEmpty) 1.0
    else 0.0

  private def compareWordSetWithSynonyms(wordSet: Set[Word], otherWordSet: Set[Word]) =
    if (wordSet.isEmpty) compareEmptyWordSet(wordSet, otherWordSet)
    else wordSet
      .map(x => x :: x.synonyms)
      .transpose
      .map(x => compareWordSet(x, otherWordSet))
      .max

  def subjects: Seq[SubjectFact] = facts.foldLeft(List[SubjectFact]()) {
    case (result, fact: SubjectFact) => fact :: result
    case (result, _) => result
  }

  def objects: Seq[ObjectFact] = facts.foldLeft(List[ObjectFact]()) {
    case (result, fact: ObjectFact) => fact :: result
    case (result, _) => result
  }

  def relations: Seq[RelationFact] = facts.foldLeft(List[RelationFact]()) {
    case (result, fact: RelationFact) => fact :: result
    case (result, _) => result
  }
}
