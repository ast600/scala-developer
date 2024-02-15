import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import opaque.ThreadPoolExample.NumThreads

import scala.runtime.RichInt

class OpaqueSpec extends AnyFlatSpec with Matchers {
  behavior of "An opaque type"
  
  it should "use methods of the underlying type if they are used within the context of it's definition" in {
    val thisPool = NumThreads(2)
    val thatPool = NumThreads(4)
    
    thisPool.merge(thatPool) should be (NumThreads(6))
  }
  
  // NumThreads(3) + 2 does not compile
}
