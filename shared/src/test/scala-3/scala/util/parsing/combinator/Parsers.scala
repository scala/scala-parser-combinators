/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc. dba Akka
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.util.parsing.combinator

import scala.language.strictEquality
import scala.util.parsing.combinator.RegexParsers

class CanEqualsCompileTest extends RegexParsers {
  val x: String ~ String = new~("a", "b")
  val y: String ~ String = new~("a", "b")
  x == y
}

