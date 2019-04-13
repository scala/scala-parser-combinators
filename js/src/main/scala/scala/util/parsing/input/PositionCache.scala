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

import java.util.Collections

private[input] trait PositionCache {
  private[input] lazy val indexCache: java.util.Map[CharSequence,Array[Int]] =
    new java.util.AbstractMap[CharSequence, Array[Int]] {
      override def entrySet() = Collections.emptySet()

      // the /dev/null of Maps
      override def put(ch: CharSequence, a: Array[Int]) = null
    }
}
