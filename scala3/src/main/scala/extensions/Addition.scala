package extensions

object Addition {
  extension (digits: String)
    def addByDecimalPlace(otherDigits: String): Long = {
      val theseOptionalDigits = MatchesRegex(raw"^[1-9]\d*".r, digits)
      val thoseOptionalDigits = MatchesRegex(raw"\d+".r, otherDigits)
      
      val optionalSum = for {
        start <- theseOptionalDigits.getOptionalValue
        end <- thoseOptionalDigits.getOptionalValue
      } yield { (start + end).toLong }
      
      optionalSum.getOrElse { throw new IllegalArgumentException("Sum on undefined digit strings") }
    }
}
