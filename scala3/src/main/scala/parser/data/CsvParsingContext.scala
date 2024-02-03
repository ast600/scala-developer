package parser.data

final case class CsvParsingContext[U](field: U, remainingCsvString: String)
