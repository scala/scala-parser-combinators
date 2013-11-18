import java.io.{File,StringReader}

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{CharArrayReader, StreamReader}

import org.junit.Test
import org.junit.Assert.assertEquals

class T0700 {
  class TestParsers extends Parsers {
    type Elem = Char

    def p: Parser[List[Int]] = rep(p1 | p2)
    def p1: Parser[Int] = 'a' ~ nl ~ 'b' ~ nl ^^^ 1
    def p2: Parser[Int] = 'a' ~ nl ^^^ 2
    def nl: Parser[Int] = rep(accept('\n') | accept('\r')) ^^^ 0
  }

  @Test
  def test: Unit = {
    val tstParsers = new TestParsers
    val s = "a\na\na"
    val r1 = new CharArrayReader(s.toCharArray())
    val r2 = StreamReader(new StringReader(s))
    assertEquals("[3.2] parsed: List(2, 2, 2)", tstParsers.p(r1).toString)
    assertEquals("[3.2] parsed: List(2, 2, 2)", tstParsers.p(r2).toString)
  }
}