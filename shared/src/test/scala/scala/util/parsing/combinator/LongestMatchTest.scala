package scala.util.parsing.combinator

import java.io.StringReader

import scala.util.parsing.input.StreamReader

import org.junit.Test
import org.junit.Assert.{ assertEquals, fail }

class LongestMatchTest {
  class TestParsers extends Parsers {
    type Elem = Char

    def ab: Parser[String] = 'a' ~ 'b' ^^^ "ab"
    def a: Parser[String] = 'a' ^^^ "a"
    def ab_alt: Parser[String] = 'a' ~ 'b' ^^^ "alt"
  }

  @Test
  def longestMatchFirst: Unit = {
    val tParsers = new TestParsers
    val reader = StreamReader(new StringReader("ab"))
    val p = tParsers.ab ||| tParsers.a
    p(reader) match {
      case tParsers.Success(result, _) => assertEquals("ab", result)
      case _ => fail()
    }
  }

  @Test
  def longestMatchSecond: Unit = {
    val tParsers = new TestParsers
    val reader = StreamReader(new StringReader("ab"))
    val p = tParsers.a ||| tParsers.ab
    p(reader) match {
      case tParsers.Success(result, _) => assertEquals("ab", result)
      case _ => fail()
    }
  }

  @Test
  def tieGoesToFirst: Unit = {
    val tParsers = new TestParsers
    val reader = StreamReader(new StringReader("ab"))
    val p = tParsers.ab ||| tParsers.ab_alt
    p(reader) match {
      case tParsers.Success(result, _) => assertEquals("ab", result)
      case _ => fail()
    }
  }
}
