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

package scala
package util.parsing
package combinator
package syntactical

import token._
import lexical.StdLexical
import scala.language.implicitConversions

/** This component provides primitive parsers for the standard tokens defined in `StdTokens`.
 */
class StandardTokenParsers extends StdTokenParsers {
  type Tokens = StdTokens
  val lexical: StdLexical = new StdLexical() // type annotation added for dotty

  //an implicit keyword function that gives a warning when a given word is not in the reserved/delimiters list
  override implicit def keyword(chars : String): Parser[String] =
    if(lexical.reserved.contains(chars) || lexical.delimiters.contains(chars)) super.keyword(chars)
    else failure("You are trying to parse \""+chars+"\", but it is neither contained in the delimiters list, nor in the reserved keyword list of your lexical object")

}
