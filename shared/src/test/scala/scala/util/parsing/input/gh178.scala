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

import org.junit.Assert.assertEquals
import org.junit.Test

class gh178 {

  @Test
  def test: Unit = {
    val len = 100000
    val i = Iterator.fill(len)("A")
    val pagedSeq = PagedSeq.fromStrings(i)
    assertEquals(len, pagedSeq.slice(0).length)  // should not fail with StackOverflowError
  }
}
