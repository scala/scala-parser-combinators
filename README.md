scala-parser-combinators [<img src="https://img.shields.io/travis/scala/scala-parser-combinators.svg"/>](https://travis-ci.org/scala/scala-parser-combinators) [<img src="https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-parser-combinators_2.11.svg?label=latest%20release%20for%202.11"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.scala-lang.modules%20a%3Ascala-parser-combinators_2.11) [<img src="https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-parser-combinators_2.12*.svg?label=latest%20release%20for%202.12"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.scala-lang.modules%20a%3Ascala-parser-combinators_2.12*) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scala/scala-parser-combinators)
========================

### Scala Standard Parser Combinator Library

This library is now community-maintained. If you are interested in helping please contact @gourlaysama or mention it [on Gitter](https://gitter.im/scala/scala-parser-combinators).

As of Scala 2.11, this library is a separate jar that can be omitted from Scala projects that do not use Parser Combinators.

#### New: completion parsers
Mixing-in the `CompletionSupport` trait enables completion support for a grammar (use `RegexCompletionSupport` for `RegexParsers`):

```scala
object MyParsers extends RegexParsers with RegexCompletionSupport
```

Parsers are thus 'augmented' with a `completions` method which returns possible entry completions for a certain input. This can be used to elaborate as-you-type completions menus or tab-completion experiences, and is e.g. easy to plug with readline to implement a console application. 
A set of additional operators also allow overriding completions and specifying ordering and grouping properties for completions. 

## Documentation

 * [Current API](https://javadoc.io/page/org.scala-lang.modules/scala-parser-combinators_2.12/latest/scala/util/parsing/combinator/index.html)
 * The [Getting Started](docs/Getting_Started.md) guide
 * A more complicated example, [Building a lexer and parser with Scala's Parser Combinators](https://enear.github.io/2016/03/31/parser-combinators/)
 * "Combinator Parsing", chapter 33 of [_Programming in Scala, Third Edition_](http://www.artima.com/shop/programming_in_scala), shows how to use this library to parse arithmetic expressions and JSON. The second half of the chapter examines how the library is implemented.

## Adding an SBT dependency
To depend on scala-parser-combinators in SBT, add something like this to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"
```

(Assuming you're using a `scalaVersion` for which a scala-parser-combinators is published. The first 2.11 milestone for which this is true is 2.11.0-M4.)

To support multiple Scala versions, see the example in https://github.com/scala/scala-module-dependency-sample.

## Example

```scala
import scala.util.parsing.combinator._

case class WordFreq(word: String, count: Int) {
    override def toString = "Word <" + word + "> " +
                            "occurs with frequency " + count
}

class SimpleParser extends RegexParsers {
    def word: Parser[String]   = """[a-z]+""".r       ^^ { _.toString }
    def number: Parser[Int]    = """(0|[1-9]\d*)""".r ^^ { _.toInt }
    def freq: Parser[WordFreq] = word ~ number        ^^ { case wd ~ fr => WordFreq(wd,fr) }
}

object TestSimpleParser extends SimpleParser {
    def main(args: Array[String]) = {
        parse(freq, "johnny 121") match {
            case Success(matched,_) => println(matched)
            case Failure(msg,_) => println("FAILURE: " + msg)
            case Error(msg,_) => println("ERROR: " + msg)
        }
    }
}
```

For a detailed unpacking of this example see
[Getting Started](docs/Getting_Started.md).

## ScalaJS support

Scala-parser-combinators directly supports scala-js 0.6+, starting with v1.0.5:

```
libraryDependencies += "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.0.6"
```

## Contributing

 * See the [Scala Developer Guidelines](https://github.com/scala/scala/blob/2.12.x/CONTRIBUTING.md) for general contributing guidelines
 * Have a look at [existing issues](https://github.com/scala/scala-parser-combinators/issues)
 * Ask questions and discuss [on Gitter](https://gitter.im/scala/scala-parser-combinators)
