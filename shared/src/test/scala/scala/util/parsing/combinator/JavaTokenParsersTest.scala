/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.util.parsing.combinator

import scala.util.parsing.input.CharArrayReader

import org.junit.Test
import org.junit.Assert.assertEquals

class JavaTokenParsersTest {

  @Test
  def parseDecimalNumber: Unit = {
    object TestJavaTokenParsers extends JavaTokenParsers
    import TestJavaTokenParsers._
    assertEquals("1.1", decimalNumber(new CharArrayReader("1.1".toCharArray)).get)
    assertEquals("1.", decimalNumber(new CharArrayReader("1.".toCharArray)).get)
    assertEquals(".1", decimalNumber(new CharArrayReader(".1".toCharArray)).get)
    // should fail to parse and we should get Failure as ParseResult
    val failure = decimalNumber(new CharArrayReader("!1".toCharArray)).asInstanceOf[Failure]
    assertEquals("""string matching regex '(\d+(\.\d*)?|\d*\.\d+)' expected but '!' found""", failure.msg)
  }

  @Test
  def parseJavaIdent: Unit = {
    object javaTokenParser extends JavaTokenParsers
    import javaTokenParser._
    def parseSuccess(s: String): Unit = {
      val parseResult = parseAll(ident, s)
      parseResult match {
        case Success(r, _) => assertEquals(s, r)
        case _ => sys.error(parseResult.toString)
      }
    }
    def parseFailure(s: String, errorColPos: Int): Unit = {
      val parseResult = parseAll(ident, s)
      parseResult match {
        case Failure(_, next) =>
          val pos = next.pos
          assertEquals(1, pos.line)
          assertEquals(errorColPos, pos.column)
        case _ => sys.error(parseResult.toString)
      }
    }
    parseSuccess("simple")
    parseSuccess("with123")
    parseSuccess("with$")
    parseSuccess("with\u00f8\u00df\u00f6\u00e8\u00e6")
    parseSuccess("with_")
    parseSuccess("_with")

    parseFailure("", 1)
    parseFailure("3start", 1)
    parseFailure("-start", 1)
    parseFailure("with-s", 5)
    // we♥scala
    parseFailure("we\u2665scala", 3)
    parseFailure("with space", 5)
  }

  @Test
  def repeatedlyParsesTest: Unit = {
    object TestTokenParser extends JavaTokenParsers
    import TestTokenParser._
    val p = ident ~ "(?i)AND".r.*

    val parseResult = parseAll(p, "start")
    parseResult match {
      case Success(r, _) =>
        assertEquals("start", r._1)
        assertEquals(0, r._2.size)
      case _ => sys.error(parseResult.toString)
    }

    val parseResult1 = parseAll(p, "start start")
    parseResult1 match {
      case Failure(message, next) =>
        assertEquals(next.pos.line, 1)
        assertEquals(next.pos.column, 7)
        assert(message.endsWith("string matching regex '(?i)AND' expected but 's' found"))
      case _ => sys.error(parseResult1.toString)
    }

    val parseResult2 = parseAll(p, "start AND AND")
    parseResult2 match {
      case Success(r, _) =>
        assertEquals("start", r._1)
        assertEquals("AND AND", r._2.mkString(" "))
      case _ => sys.error(parseResult2.toString)
    }
  }

  @Test
  def optionParserTest: Unit = {
    object TestTokenParser extends JavaTokenParsers
    import TestTokenParser._
    val p = opt(ident)

    val parseResult = parseAll(p, "-start")
    parseResult match {
      case Failure(message, next) =>
        assertEquals(next.pos.line, 1)
        assertEquals(next.pos.column, 1)
        assert(message.endsWith(s"identifier expected but '-' found"))
      case _ => sys.error(parseResult.toString)
    }

    val parseResult2 = parseAll(p, "start ")
    parseResult2 match {
      case Success(r, _) =>
        assertEquals(r, Some("start"))
      case _ =>
        sys.error(parseResult2.toString)
    }

    val parseResult3 = parseAll(p, "start")
    parseResult3 match {
      case Success(r, _) =>
        assertEquals(r, Some("start"))
      case _ => sys.error(parseResult3.toString)
    }
  }


}
