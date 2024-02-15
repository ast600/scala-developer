package opaque

object ThreadPoolExample {
  opaque type NumThreads = Int

  object NumThreads:
    def apply(n: Int): NumThreads = n
    
    
  extension (n: NumThreads)
    def merge(other: NumThreads): NumThreads = n + other
}
