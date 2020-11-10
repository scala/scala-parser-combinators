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

/** This is the core component for token-based parsers.
 */
trait TokenParsers extends Parsers {
  /** `Tokens` is the abstract type of the `Token`s consumed by the parsers in this component. */
  type Tokens <: token.Tokens

  /** `lexical` is the component responsible for consuming some basic kind of
   *  input (usually character-based) and turning it into the tokens
   *  understood by these parsers.
   */
  val lexical: Tokens

  /** The input-type for these parsers*/
  type Elem = lexical.Token

}


