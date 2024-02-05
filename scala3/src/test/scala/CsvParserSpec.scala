import music.Album
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import parser.CsvParserMonadic
import parser.CsvParserMonadic.Splitter
import parser.givens.CsvGivens.given

class CsvParserSpec extends AnyFlatSpec with Matchers {
  private def string2Album(csvString: String)(using parser: CsvParserMonadic[Album]) = parser.getResult(csvString)

  "An album string" should "be converted to an album instance with correct structure" in {
    given sep: Splitter = Splitter(";")

    val AlbumAsCsvString =
      """1990;Pantera;Cowboys from Hell;
        |Primal Concrete Sledge,
        |Psycho Holiday,
        |Heresy,
        |Cemetery Gates,
        |Domination,Shattered,
        |Clash with Reality,
        |Medicine Man,
        |Message In Blood,
        |The Sleep,
        |The Art of Shredding;""".stripMargin.replaceAll("\n", "")
    val album = string2Album(AlbumAsCsvString)
    
    album.year should be (1990)
    album.bandName should be ("Pantera")
    album.albumName should be ("Cowboys from Hell")
    album.trackList should have (size(11))
  }
}
