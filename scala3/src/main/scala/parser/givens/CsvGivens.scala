package parser.givens

import music.Album
import parser.CsvParserMonadic
import parser.CsvParserMonadic.Splitter
import parser.data.CsvParsingContext

object CsvGivens {
  given StringCsvParserMonadic(using splitter: Splitter): CsvParserMonadic[String] = CsvParserMonadic
    .basicParserFromContextualSeparator

  given IntCsvParserMonadic(using splitter: Splitter): CsvParserMonadic[Int] = StringCsvParserMonadic.map { _.toInt }

  given ListCsvParserMonadic[T](using splitter: Splitter, elemParser: CsvParserMonadic[T]): CsvParserMonadic[List[T]] =
    for {
      elems <- StringCsvParserMonadic.map { _.split(",").toList }
      result = elems.map { e => elemParser.getResult(e) }
    } yield result

  given AlbumCsvParserMonadic(using intParser: CsvParserMonadic[Int],
                              stringParser: CsvParserMonadic[String],
                              listOfStringsParser: CsvParserMonadic[List[String]]): CsvParserMonadic[Album] =
    for {
      year <- intParser
      band <- stringParser
      name <- stringParser
      trackList <- listOfStringsParser
    } yield Album(year, band, name, trackList)
}
