package checker

sealed trait TomitaFactValue {
  def value: String
}

case class ObjectVal(value: String) extends TomitaFactValue
case class CanonicWordVal(value: String) extends TomitaFactValue
case class RelationVal(value: String) extends TomitaFactValue
case class SubjectVal(value: String) extends TomitaFactValue

sealed trait TomitaFact

case class ObjectFact(value: TomitaFactValue) extends TomitaFact
case class SubjectFact(value: TomitaFactValue) extends TomitaFact
case class RelationFact(value: TomitaFactValue) extends TomitaFact
case class OtherTag(value: TomitaFactValue) extends TomitaFact
case class VerbTag(value: TomitaFactValue) extends TomitaFact
case class AdjTag(value: TomitaFactValue) extends TomitaFact
case class NounTag(value: TomitaFactValue) extends TomitaFact