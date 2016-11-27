package scala.util.parsing.combinator

import scala.util.parsing.input._
import scala.collection.immutable.PagedSeq

import org.junit.Test
import org.junit.Assert.assertTrue

import scala.util.parsing.combinator.syntactical.StandardTokenParsers

class gh45 {

  @Test
  def test4: Unit = {
    def check(rd: Reader[Char, Position]): Unit = {
      val g = new grammar
      val p = g.phrase(g.script)
      val parseResult = p(new g.lexical.Scanner(rd))
      assertTrue(parseResult.isInstanceOf[g.Success[_]])
    }

    val str = "x once y"
    check(new CharSequenceReader(str))
    /* Note that this only tests PagedSeq.rest since neither
     * PackratReader nor lexical.Scanner override/use the drop method.
     */
    check(new PagedSeqReader(PagedSeq.fromStrings(List(str))))
  }

}

private final class grammar extends StandardTokenParsers with PackratParsers {
  lexical.reserved ++= List("x", "y", "z", "once")

  var onceCnt: Int = 0
  lazy val once: PackratParser[String] = memo("once") ^? {
    case s if onceCnt == 0 =>
      onceCnt += 1
      s
  }

  lazy val script: PackratParser[Any] =
    ( "x" ~ once ~ "z"
    | "x" ~ once ~ "y"
    )
}
