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

import org.junit.Assert.assertEquals
import org.junit.Test

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.CharSequenceReader

class gh242 {
  class TestWithSeparator extends Parsers {
    type Elem = Char
    val csv: Parser[List[Char]] = repNM(5, 10, 'a', ',')
  }

  class TestWithoutSeparator extends Parsers {
    type Elem = Char
    val csv: Parser[List[Char]] = repNM(5, 10, 'a')
  }

  @Test
  def testEmpty(): Unit = {
    val tstParsers = new TestWithSeparator
    val s = new CharSequenceReader("")
    val expectedFailure = """[1.1] failure: end of input
                            |
                            |
                            |^""".stripMargin
    assertEquals(expectedFailure, tstParsers.csv(s).toString)
  }

  @Test
  def testBelowMinimum(): Unit = {
    val tstParsers = new TestWithSeparator
    val s = new CharSequenceReader("a,a,a,a")
    val expectedFailure = """[1.8] failure: end of input
                            |
                            |a,a,a,a
                            |       ^""".stripMargin
    assertEquals(expectedFailure, tstParsers.csv(s).toString)
  }

  @Test
  def testMinimum(): Unit = {
    val tstParsers = new TestWithSeparator
    val s = new CharSequenceReader("a,a,a,a,a")
    val expected = List.fill[Char](5)('a')
    val actual = tstParsers.csv(s)
    assertEquals(9, actual.next.offset)
    assert(actual.successful)
    assertEquals(expected, actual.get)
  }

  @Test
  def testInRange(): Unit = {
    val tstParsers = new TestWithSeparator
    val s = new CharSequenceReader("a,a,a,a,a,a,a,a")
    val expected = List.fill[Char](8)('a')
    val actual = tstParsers.csv(s)
    assertEquals(15, actual.next.offset)
    assert(actual.successful)
    assertEquals(expected, actual.get)
  }

  @Test
  def testMaximum(): Unit = {
    val tstParsers = new TestWithSeparator
    val s = new CharSequenceReader("a,a,a,a,a,a,a,a,a,a")
    val expected = List.fill[Char](10)('a')
    val actual = tstParsers.csv(s)
    assertEquals(19, actual.next.offset)
    assert(actual.successful)
    assertEquals(expected, actual.get)
  }

  @Test
  def testAboveMaximum(): Unit = {
    val tstParsers = new TestWithSeparator
    val s = new CharSequenceReader("a,a,a,a,a,a,a,a,a,a,a,a")
    val expected = List.fill[Char](10)('a')
    val actual = tstParsers.csv(s)
    assertEquals(19, actual.next.offset)
    assert(actual.successful)
    assertEquals(expected, actual.get)
  }

  @Test
  def testEmptyWithoutSep(): Unit = {
    val tstParsers = new TestWithoutSeparator
    val s = new CharSequenceReader("")
    val expectedFailure = """[1.1] failure: end of input
                            |
                            |
                            |^""".stripMargin
    assertEquals(expectedFailure, tstParsers.csv(s).toString)
  }

  @Test
  def testBelowMinimumWithoutSep(): Unit = {
    val tstParsers = new TestWithoutSeparator
    val s = new CharSequenceReader("aaaa")
    val expectedFailure = """[1.5] failure: end of input
                            |
                            |aaaa
                            |    ^""".stripMargin
    assertEquals(expectedFailure, tstParsers.csv(s).toString)
  }

  @Test
  def testMinimumWithoutSep(): Unit = {
    val tstParsers = new TestWithoutSeparator
    val s = new CharSequenceReader("aaaaa")
    val expected = List.fill[Char](5)('a')
    val actual = tstParsers.csv(s)
    assertEquals(5, actual.next.offset)
    assert(actual.successful)
    assertEquals(expected, actual.get)
  }

  @Test
  def testInRangeWithoutSep(): Unit = {
    val tstParsers = new TestWithoutSeparator
    val s = new CharSequenceReader("aaaaaaaa")
    val expected = List.fill[Char](8)('a')
    val actual = tstParsers.csv(s)
    assertEquals(8, actual.next.offset)
    assert(actual.successful)
    assertEquals(expected, actual.get)
  }

  @Test
  def testMaximumWithoutSep(): Unit = {
    val tstParsers = new TestWithoutSeparator
    val s = new CharSequenceReader("aaaaaaaaaa")
    val expected = List.fill[Char](10)('a')
    val actual = tstParsers.csv(s)
    assertEquals(10, actual.next.offset)
    assert(actual.successful)
    assertEquals(expected, actual.get)
  }

  @Test
  def testAboveMaximumWithoutSep(): Unit = {
    val tstParsers = new TestWithoutSeparator
    val s = new CharSequenceReader("aaaaaaaaaaaa")
    val expected = List.fill[Char](10)('a')
    val actual = tstParsers.csv(s)
    assertEquals(10, actual.next.offset)
    assert(actual.successful)
    assertEquals(expected, actual.get)
  }
}
