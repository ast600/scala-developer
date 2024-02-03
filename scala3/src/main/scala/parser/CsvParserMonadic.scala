package parser

import parser.CsvParserMonadic.Splitter
import parser.data.CsvParsingContext

case class CsvParserMonadic[T](private val parse: String => CsvParsingContext[T]) {
  def flatMap[R](f: T => CsvParserMonadic[R]): CsvParserMonadic[R] = CsvParserMonadic { sourceString =>
    val intermediateContext = parse(sourceString)
    val monadicParserFromField = f(intermediateContext.field)
    monadicParserFromField.parse(intermediateContext.remainingCsvString)
  }
  
  def map[S](g: T => S): CsvParserMonadic[S] = CsvParserMonadic { sourceString =>
    val context = parse(sourceString)
    CsvParsingContext(g(context.field), context.remainingCsvString)
  }
  
  def getResult(inputCsvString: String): T = parse(inputCsvString).field
}

object CsvParserMonadic {
  
  opaque type Splitter = String
  
  object Splitter {
    def apply(value: String): Splitter = value
  }
  
  private[parser] def basicParserFromContextualSeparator(using splitter: Splitter): CsvParserMonadic[String] = {
    val parserFunc = { (str: String) =>
      val partitionedString = str.split(s"$splitter", 2)
      CsvParsingContext(partitionedString.head, partitionedString.last)
    }
    
    CsvParserMonadic(parserFunc)
  }
}
