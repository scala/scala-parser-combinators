package scala.util.parsing.combinator

// import scala.language.implicitConversions

import java.io.StringReader
import org.junit.Test
import org.junit.Assert.{ assertEquals, assertTrue, fail }
import scala.util.parsing.input.StreamReader

class PrecedenceParsersTest {

  abstract class Op
  object Plus extends Op {
    override def toString = "+"
  }
  object Minus extends Op {
    override def toString = "-"
  }
  object Mult extends Op {
    override def toString = "*"
  }
  object Divide extends Op {
    override def toString = "/"
  }
  object Equals extends Op {
    override def toString = "="
  }

  abstract class Node
  case class Leaf(v: Int) extends Node {
    override def toString = v.toString
  }
  case class Binop(lhs: Node, op: Op, rhs: Node) extends Node {
    override def toString = s"($lhs $op $rhs)"
  }

  object ArithmeticParser extends RegexParsers {
    val prec = List(
      (Associativity.Left, List(Mult, Divide)),
      (Associativity.Left, List(Plus, Minus)),
      (Associativity.Right, List(Equals)))
    def integer: Parser[Leaf] = "[0-9]+".r ^^ { s: String => Leaf(s.toInt) }
    def binop: Parser[Op] = "+" ^^^ Plus | "-" ^^^ Minus | "*" ^^^ Mult | "/" ^^^ Divide | "=" ^^^ Equals
    def expression = new PrecedenceParser(integer, binop, prec, Binop.apply)
  }

  def testExp(expected: Node, input: String): Unit = {
    ArithmeticParser.expression(StreamReader(new StringReader(input))) match {
      case ArithmeticParser.Success(r, next) => {
        assertEquals(expected, r);
        assertTrue(next.atEnd);
      }
      case e => {
        fail(s"Error parsing $input: $e");
      }
    }
  }

  @Test
  def basicExpTests: Unit = {
    testExp(Leaf(4), "4")
    testExp(Binop(Leaf(1), Plus, Leaf(2)), "1 + 2")
    testExp(Binop(Leaf(2), Mult, Leaf(1)), "2 * 1")
  }

  @Test
  def associativityTests: Unit = {
    testExp(Binop(Binop(Leaf(1), Minus, Leaf(2)), Plus, Leaf(3)), "1 - 2 + 3")
    testExp(Binop(Leaf(1), Equals, Binop(Leaf(2), Equals, Leaf(3))), "1 = 2 = 3")
  }

  @Test
  def precedenceTests: Unit = {
    testExp(Binop(Binop(Leaf(0), Mult, Leaf(5)), Minus, Leaf(2)), "0 * 5 - 2")
    testExp(Binop(Leaf(3), Plus, Binop(Leaf(9), Divide, Leaf(11))), "3 + 9 / 11")
    testExp(Binop(Binop(Leaf(6), Plus, Leaf(8)), Equals, Leaf(1)), "6 + 8 = 1")
    testExp(Binop(Leaf(4), Equals, Binop(Leaf(5), Minus, Leaf(3))), "4 = 5 - 3")
  }
}
