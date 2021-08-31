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

import org.junit.Assert._
import org.junit.Test

class gh64 {

  @Test
  def test: Unit = {
    val len = 4096 * 20000
    val i = Iterator.fill(len)(true)  // use `true` to make this test more lightweight
    val pagedSeq = PagedSeq.fromIterator(i)
    pagedSeq.slice(len - 1)  // load the whole pagedSeq without caching `latest` element
    assertEquals(len, pagedSeq.length)  // should not throw StackOverflowError
  }
}
