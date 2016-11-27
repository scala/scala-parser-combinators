import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{CharArrayReader, CharSequenceReader, Position, StreamReader}
import java.io.{File, StringReader}

import scala.util.parsing.combinator.Parsers
import org.junit.Test
import org.junit.Assert.assertEquals

class T1100 {

  class TestParsers extends Parsers {
    type Elem = Char
    type Pos = Position

    def p: Parser[List[Char]] = rep1(p1)

    def p1: Parser[Char] = accept('a') | err("errors are propagated")
  }

  val expected =
    """[1.4] error: errors are propagated

aaab
   ^"""

  @Test
  def test(): Unit = {
    val tstParsers = new TestParsers
    val s = new CharSequenceReader("aaab")
    assertEquals(expected, tstParsers.p(s).toString)
  }
}
