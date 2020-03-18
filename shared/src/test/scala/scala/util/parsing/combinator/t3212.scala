package scala.util.parsing.combinator

import org.junit.Test
import org.junit.Assert.assertEquals
import scala.language.implicitConversions

class t3212 extends RegexParsers {

  sealed trait BuySell
  case object BUY extends BuySell
  case object SELL extends BuySell

  def buy_sell: Parser[BuySell] =
    "to" ~> "buy" ^^^ BUY |
      "to" ~> "sell" ^^^ SELL |
      failure("buy or sell expected")

  @Test
  def test: Unit = {
    val parseResult = parse[BuySell](phrase(buy_sell), "bought")

    val expected = """[1.1] failure: buy or sell expected

bought
^"""
    assertEquals(expected, parseResult.toString)
  }
}
