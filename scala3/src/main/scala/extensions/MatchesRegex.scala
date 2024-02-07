package extensions

import scala.util.matching.Regex

case class MatchesRegex(regex: Regex, private val inputString: String) {

  private val optionalValue: Option[String] = {
    if (regex.matches(inputString)) Some(inputString) else Option.empty[String]
  }
  def getOptionalValue: Option[String] = optionalValue
}