package checker

class TomitaException(val message: String) extends Exception {
  override def getMessage: String = message
}
