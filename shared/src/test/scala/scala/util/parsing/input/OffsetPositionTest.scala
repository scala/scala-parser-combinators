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

package scala.util.parsing.input

import org.junit.Test
import org.junit.Assert.assertEquals

class OffsetPositionTest {
  @Test
  def lineContentsWithTrailingLF: Unit = {
    val op = new OffsetPosition("\n", 1)
    assertEquals("", op.lineContents)
  }

  @Test
  def lineContentsWithTrailingCR: Unit = {
    val op = new OffsetPosition("\r", 1)
    assertEquals("", op.lineContents)
  }

  @Test
  def lineContentsWithTrailingCRLF: Unit = {
    val op = new OffsetPosition("\r\n", 2)
    assertEquals("", op.lineContents)
  }

  @Test
  def lineContentsWithEmptySource: Unit = {
    val op = new OffsetPosition("", 0)
    assertEquals("", op.lineContents)
  }

  @Test
  def linesWithLF: Unit = {
    val op = new OffsetPosition("foo\nbar", 4)
    assertEquals(2, op.line)
  }

  @Test
  def linesWithCR: Unit = {
    val op = new OffsetPosition("foo\rbar", 4)
    assertEquals(2, op.line)
  }

  @Test
  def linesWithCRLF: Unit = {
    val op = new OffsetPosition("foo\r\nbar", 5)
    assertEquals(2, op.line)
  }

  @Test
  def linesWithTrailingLFs: Unit = {
    val op = new OffsetPosition("foo\n\n", 5)
    assertEquals(3, op.line)
  }

  @Test
  def linesWithTrailingCRs: Unit = {
    val op = new OffsetPosition("foo\r\r", 5)
    assertEquals(3, op.line)
  }

  @Test
  def linesWithTrailingCRLFs: Unit = {
    val op = new OffsetPosition("foo\r\n\r\n", 7)
    assertEquals(3, op.line)
  }

  @Test
  def linesWithLeadingLF: Unit = {
    val op = new OffsetPosition("\n", 1)
    assertEquals(2, op.line)
  }

  @Test
  def linesWithLeadingCR: Unit = {
    val op = new OffsetPosition("\r", 1)
    assertEquals(2, op.line)
  }

  @Test
  def linesWithLeadingCRLF: Unit = {
    val op = new OffsetPosition("\r\n", 2)
    assertEquals(2, op.line)
  }
}
