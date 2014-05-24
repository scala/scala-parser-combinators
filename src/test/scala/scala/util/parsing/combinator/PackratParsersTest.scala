package scala.util.parsing.combinator

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

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
    def check(expected: Int, expr: String): Unit = {
      val parseResult = head(new lexical.Scanner(expr))
      val result = extractResult(parseResult)
      assertEquals(expected, result)
    }

    check(1, "1")
    check(3, "1+2")
    check(5, "9-4")
    check(81, "9*9")
    check(4, "8/2")
    check(37, "4*9-0/7+9-8*1")
    check(9, "(1+2)*3")
    check(3, """/* This is a
                   long long long long long 
                   long long long long long
                   long long long long long
                   long long long long long 
                   long long long long long
                   long long long long long
                   long long long long long 
                   long long long long long
                   long long long long long
                   long long long long long 
                   long long long long long
                   long long long long long
                   long long long long long 
                   long long long long long
                   long long long long long
                   long long long long long 
                   long long long long long
                   long long long long long
                   long long long long long 
                   long long long long long
                   long long long long long
                   long long long long long 
                   long long long long long
                   long long long long long
                   long long long long long 
                   long long long long long
                   long long long long long
                   comment */
                1+2""")
  }

  @Test
  def test2: Unit = {
    import grammars2._
    val head = phrase(exp)

    def extractResult(r : ParseResult[Int]): Int = r match {
      case Success(a,_) => a
      case NoSuccess(a,_) => sys.error(a)
    }
    def check(expected: Int, expr: String): Unit = {
      val parseResult = head(new lexical.Scanner(expr))
      val result = extractResult(parseResult)
      assertEquals(expected, result)
    }

    check(1, "1")
    check(3, "1+2")
    check(81, "9*9")
    check(43, "4*9+7")
    check(59, "4*9+7*2+3*3")
    check(188, "4*9+7*2+3*3+9*5+7*6*2")
    check(960, "4*(9+7)*(2+3)*3")
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
    def assertSuccess(expected1: List[Symbol], expected2: List[Symbol], expected3: List[Symbol],
        input: String): Unit = {
      val expected = threeLists(expected1, expected2, expected3)
      val parseResult = head(new lexical.Scanner(input))
      val result = extractResult(parseResult)
      assertEquals(expected, result)
    }

    assertSuccess(List('a, 'b), List('a), List('b, 'c), "a b c")
    assertSuccess(List('a, 'a, 'b, 'b), List('a, 'a), List('b, 'b, 'c, 'c), "a a b b c c")
    assertSuccess(List('a, 'a, 'a, 'b, 'b, 'b), List('a, 'a, 'a), List('b, 'b, 'b, 'c, 'c, 'c),
      "a a a b b b c c c")
    assertSuccess(List('a, 'a, 'a, 'a, 'b, 'b, 'b, 'b), List('a, 'a, 'a, 'a), List('b, 'b, 'b, 'b, 'c, 'c, 'c, 'c),
      "a a a a b b b b c c c c")

    def assertFailure(expectedFailureMsg: String, input: String): Unit = {
      val packratReader = new PackratReader(new lexical.Scanner(input))
      val parseResult = AnBnCn(packratReader)
      assertTrue(s"Not an instance of Failure: ${parseResult.toString()}", parseResult.isInstanceOf[Failure])
      val failure = parseResult.asInstanceOf[Failure]
      assertEquals(expectedFailureMsg, failure.msg)
    }
    assertFailure("``b'' expected but `c' found", "a a a a b b b c c c c")
    assertFailure("end of input", "a a a a b b b b c c c")
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
