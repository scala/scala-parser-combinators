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

package scala.util.parsing.combinator.lexical

import org.junit.Test
import org.junit.Assert.assertEquals

import scala.util.parsing.input.Reader

import scala.collection.mutable.ListBuffer

class StdLexicalTest {
  private def lex[Lexer <: StdLexical](lexer: Lexer, input: String): List[lexer.Token] = {
    var scanner: Reader[lexer.Token] = new lexer.Scanner(input)
    val listBuffer = ListBuffer[lexer.Token]()
    while (!scanner.atEnd) {
      listBuffer += scanner.first
      scanner = scanner.rest
    }
    listBuffer.toList
  }

  @Test
  def parseKeyword: Unit = {
    object Lexer extends StdLexical
    Lexer.reserved add "keyword"
    import Lexer._
    assertEquals(
      List(Keyword("keyword"), Identifier("id")),
      lex(Lexer, "keyword id")
    )
  }

  @Test
  def parseDelimiters: Unit = {
    object Lexer extends StdLexical
    Lexer.delimiters ++= List("(", ")", "=>")
    import Lexer._
    assertEquals(
      List(Keyword("("), Identifier("id1"), Keyword(")"), Keyword("=>"), Identifier("id2")),
      lex(Lexer, "(id1) => id2")
    )
  }

  @Test
  def parseNumericLiterals: Unit = {
    object Lexer extends StdLexical
    import Lexer._
    assertEquals(
      List(NumericLit("1"), NumericLit("21"), NumericLit("321")),
      lex(Lexer, " 1 21 321 ")
    )
  }

  @Test
  def parseStringLiterals: Unit = {
    object Lexer extends StdLexical
    import Lexer._
    assertEquals(
      List(StringLit("double double"), StringLit("single single"), StringLit("double'double"), StringLit("single\"single")),
      lex(Lexer, """
                    "double double"
                    'single single'
                    "double'double"
                    'single"single'
                 """)
    )
  }

  @Test
  def parseUnclosedStringLiterals: Unit = {
    object Lexer extends StdLexical
    import Lexer._

    // Unclosed double quoted string at end of input.
    assertEquals(
      List(Identifier("id"), ErrorToken("unclosed string literal")),
      lex(Lexer, """id """")
    )

    // Unclosed single quoted string at end of input.
    assertEquals(
      List(Identifier("id"), ErrorToken("unclosed string literal")),
      lex(Lexer, "id '")
    )

    // Unclosed double quoted string _not_ at end of input.
    assertEquals(
      List(Identifier("id"), ErrorToken("unclosed string literal")),
      lex(Lexer, """id "string""")
    )

    // Unclosed single quoted string _not_ at end of input.
    assertEquals(
      List(Identifier("id"), ErrorToken("unclosed string literal")),
      lex(Lexer, "id 'string")
    )
  }

  @Test
  def parseIllegalCharacter: Unit = {
    object Lexer extends StdLexical
    import Lexer._
    assertEquals(
      List(Identifier("we"), ErrorToken("illegal character"), Identifier("scala")),
      lex(Lexer, "we\u2665scala")
    )
  }

  @Test
  def parseComments: Unit = {
    object Lexer extends StdLexical
    import Lexer._

    // Single-line comments.
    assertEquals(
      List(Identifier("id")),
      lex(Lexer, "//\n// comment\nid // ")
    )

    // Multi-line comments.
    assertEquals(
      List(Identifier("id1"), Identifier("id2")),
      lex(Lexer, "/* single */ id1 /* multi \n line */ id2")
    )
  }

  @Test
  def parseUnclosedComments: Unit = {
    object Lexer extends StdLexical
    import Lexer._

    assertEquals(
      List(Identifier("id"), ErrorToken("unclosed comment")),
      lex(Lexer, "id /*")
    )

    assertEquals(
      List(Identifier("id"), ErrorToken("unclosed comment")),
      lex(Lexer, "id /* ")
    )
  }
}
