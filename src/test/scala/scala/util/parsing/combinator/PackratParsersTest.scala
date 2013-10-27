package scala.util.parsing.combinator

import org.junit.Test
import org.junit.Assert.assertEquals

import scala.util.parsing.combinator.syntactical.StandardTokenParsers

class PackratParsersTest {

  @Test
  def test1: Unit = {
    import grammars1._
    val head = phrase(term)

    def extractResult(r : ParseResult[Int]): Int = r match {
      case Success(a,_) => a
      case NoSuccess(a,_) => sys.error(a)
    }

    assertEquals(1,extractResult(head(new lexical.Scanner("1"))))
    assertEquals(3, extractResult(head(new lexical.Scanner("1+2"))))
    assertEquals(5, extractResult(head(new lexical.Scanner("9-4"))))
    assertEquals(81, extractResult(head(new lexical.Scanner("9*9"))))
    assertEquals(4, extractResult(head(new lexical.Scanner("8/2"))))
    assertEquals(37, extractResult(head(new lexical.Scanner("4*9-0/7+9-8*1"))))
    assertEquals(9, extractResult(head(new lexical.Scanner("(1+2)*3"))))
  }

  @Test
  def test2: Unit = {
    import grammars2._
    val head = phrase(exp)

    def extractResult(r : ParseResult[Int]): Int = r match {
      case Success(a,_) => a
      case NoSuccess(a,_) => sys.error(a)
    }

    assertEquals(1, extractResult(head(new lexical.Scanner("1"))))
    assertEquals(3, extractResult(head(new lexical.Scanner("1+2"))))
    assertEquals(81, extractResult(head(new lexical.Scanner("9*9"))))
    assertEquals(43, extractResult(head(new lexical.Scanner("4*9+7"))))
    assertEquals(59, extractResult(head(new lexical.Scanner("4*9+7*2+3*3"))))
    assertEquals(188, extractResult(head(new lexical.Scanner("4*9+7*2+3*3+9*5+7*6*2"))))
    assertEquals(960, extractResult(head(new lexical.Scanner("4*(9+7)*(2+3)*3"))))
  }

  @Test
  def test3: Unit = {
    import grammars3._
    val head = phrase(AnBnCn)
    def extractResult(r: ParseResult[AnBnCnResult]): AnBnCnResult = r match {
      case Success(a,_) => a
      case NoSuccess(a,_) => sys.error(a)
    }
    def threeLists(as: List[Symbol], bs: List[Symbol], cs: List[Symbol]): AnBnCnResult = {
      val as1 = as.map(_.name)
      val bs1 = bs.map(_.name)
      val cs1 = cs.map(_.name)
      new ~(new ~(as1, bs1), cs1)
    }

    val expected1 = threeLists(List('a, 'b), List('a), List('b, 'c))
    assertEquals(expected1, extractResult(head(new lexical.Scanner("a b c"))))
    val expected2 = threeLists(List('a, 'a, 'b, 'b), List('a, 'a), List('b, 'b, 'c, 'c))
    assertEquals(expected2, extractResult(head(new lexical.Scanner("a a b b c c"))))
    val expected3 = threeLists(List('a, 'a, 'a, 'b, 'b, 'b), List('a, 'a, 'a), List('b, 'b, 'b, 'c, 'c, 'c))
    assertEquals(expected3, extractResult(head(new lexical.Scanner("a a a b b b c c c"))))
    val expected4 = threeLists(List('a, 'a, 'a, 'a, 'b, 'b, 'b, 'b), List('a, 'a, 'a, 'a), List('b, 'b, 'b, 'b, 'c, 'c, 'c, 'c))
    assertEquals(expected4, extractResult(head(new lexical.Scanner("a a a a b b b b c c c c"))))
    val failure1 = AnBnCn(new PackratReader(new lexical.Scanner("a a a b b b b c c c c"))).asInstanceOf[Failure]
    assertEquals("Expected failure", failure1.msg)
    val failure2 = AnBnCn(new PackratReader(new lexical.Scanner("a a a a b b b c c c c"))).asInstanceOf[Failure]
    assertEquals("``b'' expected but `c' found", failure2.msg)
    val failure3 = AnBnCn(new PackratReader(new lexical.Scanner("a a a a b b b b c c c"))).asInstanceOf[Failure]
    assertEquals("end of input", failure3.msg)
  }

}

private object grammars1 extends StandardTokenParsers with PackratParsers {

  lexical.delimiters ++= List("+","-","*","/","(",")")
  lexical.reserved ++= List("Hello","World")

  /****
   * term = term + fact | term - fact | fact
   * fact = fact * num  | fact / num  | num
   */


 val term: PackratParser[Int] = (term~("+"~>fact) ^^ {case x~y => x+y}
           |term~("-"~>fact) ^^ {case x~y => x-y}
           |fact)

 val fact: PackratParser[Int] = (fact~("*"~>numericLit) ^^ {case x~y => x*y.toInt}
           |fact~("/"~>numericLit) ^^ {case x~y => x/y.toInt}
           |"("~>term<~")"
           |numericLit ^^ {_.toInt})
}

private object grammars2 extends StandardTokenParsers with PackratParsers {

  lexical.delimiters ++= List("+","-","*","/","(",")")
  lexical.reserved ++= List("Hello","World")

  /*
   * exp = sum | prod | num
   * sum = exp ~ "+" ~ num
   * prod = exp ~ "*" ~ num
   */

  val exp : PackratParser[Int] = sum | prod | numericLit ^^{_.toInt} | "("~>exp<~")"
  val sum : PackratParser[Int] = exp~("+"~>exp) ^^ {case x~y => x+y}
  val prod: PackratParser[Int] = exp~("*"~>(numericLit ^^{_.toInt} | exp)) ^^ {case x~y => x*y}

}

private object grammars3 extends StandardTokenParsers with PackratParsers {
  lexical.reserved ++= List("a","b", "c")
  val a: PackratParser[String] = memo("a")
  val b: PackratParser[String] = memo("b")
  val c: PackratParser[String] = memo("c")

  type AnBnCnResult = List[String] ~ List[String] ~ List[String]

  val AnBnCn: PackratParser[AnBnCnResult] =
    guard(repMany1(a,b) <~ not(b)) ~ rep1(a) ~ repMany1(b,c)// ^^{case x~y => x:::y}


  private def repMany[T](p: => Parser[T], q: => Parser[T]): Parser[List[T]] =
  ( p~repMany(p,q)~q ^^ {case x~xs~y => x::xs:::(y::Nil)}
   | success(Nil)
  )

  def repMany1[T](p: => Parser[T], q: => Parser[T]): Parser[List[T]] =
   p~opt(repMany(p,q))~q ^^ {case x~Some(xs)~y => x::xs:::(y::Nil)}

}
