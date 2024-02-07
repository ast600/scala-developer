package conversions

object ShowCompletionArg {
  def show(arg: CompletionArg): String = { 
    val innerValue = arg match
      case CompletionArg.Error(errorString) => errorString
      case CompletionArg.StatusCode(code) => code

    s"Got ${arg.getClass.getSimpleName} with value \"$innerValue\" of type ${innerValue.getClass.getSimpleName}"
  }
}
