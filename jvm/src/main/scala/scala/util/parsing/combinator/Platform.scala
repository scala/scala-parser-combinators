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

private[combinator] object Platform {
  // possessive quantifiers keep java.util.regex from recursing per character or escape (#371)
  val stringLiteralPattern =
    """"[^"\x00-\x1F\x7F\\]*+(?:(?:\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})[^"\x00-\x1F\x7F\\]*+)*+""""
}
