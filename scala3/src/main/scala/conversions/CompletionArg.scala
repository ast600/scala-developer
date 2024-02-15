package conversions

enum CompletionArg:
  case Error(errorString: String)
  case StatusCode(code: Int)
  
object CompletionArg {
  given fromString: Conversion[String, CompletionArg] = Error(_)
  given fromInt: Conversion[Int, CompletionArg] = StatusCode(_)

  def show(arg: CompletionArg): String = {
    val innerValue = arg match
      case CompletionArg.Error(errorString) => errorString
      case CompletionArg.StatusCode(code) => code

    s"Got ${ arg.getClass.getSimpleName } with value \"$innerValue\" of type ${ innerValue.getClass.getSimpleName }"
  }
}
