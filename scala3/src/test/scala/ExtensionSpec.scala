import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ExtensionSpec extends AnyFlatSpec with Matchers {
  import extensions.Addition.addByDecimalPlace
  
  behavior of "An extension sum method"
  
  it should "add decimal places on valid digit strings" in {
    val validStart = "56"
    val validEnd = "3"
    
    validStart addByDecimalPlace validEnd should be (563L)
  }
  
  it should "throw an IllegalArgumentException if the first string starts with zeros" in {
    val startsWithZeros = "007"
    val validEnd = "3"
    
    assertThrows[IllegalArgumentException] {
      startsWithZeros addByDecimalPlace validEnd
    }
  }
  
  it should "throw an IllegalArgumentException if any of two strings contains any symbols other than digits" in {
    val withDot = ".123"
    val validEnd = "3"
    
    val validStart = "56"
    val withLetters = "foo"
    
    assertThrows[IllegalArgumentException] {
      withDot addByDecimalPlace validEnd
    }
    assertThrows[IllegalArgumentException] {
      validStart addByDecimalPlace withLetters
    }
  }
}
