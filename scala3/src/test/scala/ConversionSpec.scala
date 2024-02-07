import conversions.ShowCompletionArg
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConversionSpec extends AnyFlatSpec with Matchers {
  behavior of "CompletionArg implicit conversions"
  
  it should "return an Error instance with an error string if a string is given to show method of ShowCompletionArg" in {
    val printableString = ShowCompletionArg.show("something wrong")
    
    printableString should be ("Got Error with value \"something wrong\" of type String")
  }

  it should "return a StatusCode instance with a code if an Int is given to show method of ShowCompletionArg" in {
    val printableString = ShowCompletionArg.show(403)

    printableString should be("Got StatusCode with value \"403\" of type Integer")
  }
}
