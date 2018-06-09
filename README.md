scala-parser-combinators [<img src="https://img.shields.io/travis/scala/scala-parser-combinators.svg"/>](https://travis-ci.org/scala/scala-parser-combinators) [<img src="https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-parser-combinators_2.11.svg?label=latest%20release%20for%202.11"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.scala-lang.modules%20a%3Ascala-parser-combinators_2.11) [<img src="https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-parser-combinators_2.12.svg?label=latest%20release%20for%202.12"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.scala-lang.modules%20a%3Ascala-parser-combinators_2.12) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scala/scala-parser-combinators)
========================

### Scala Standard Parser Combinator Library

This library is now community-maintained. If you are interested in helping please contact @gourlaysama or mention it [on Gitter](https://gitter.im/scala/scala-parser-combinators).

As of Scala 2.11, this library is a separate jar that can be omitted from Scala projects that do not use Parser Combinators.

## Documentation

 * [Current API](https://javadoc.io/page/org.scala-lang.modules/scala-parser-combinators_2.12/latest/scala/util/parsing/combinator/index.html)
 * The [Getting Started](docs/Getting_Started.md) guide
 * A more complicated example, [Building a lexer and parser with Scala's Parser Combinators](https://enear.github.io/2016/03/31/parser-combinators/)
 * "Combinator Parsing", chapter 33 of [_Programming in Scala, Third Edition_](http://www.artima.com/shop/programming_in_scala), shows how to use this library to parse arithmetic expressions and JSON. The second half of the chapter examines how the library is implemented.

## Adding an SBT dependency
To depend on scala-parser-combinators in SBT, add something like this to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.1"
```

To support multiple Scala versions, see the example in [scala/scala-module-dependency-sample](https://github.com/scala/scala-module-dependency-sample).

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

Scala-parser-combinators directly supports Scala.js 0.6+:

```
libraryDependencies += "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.1.1"
```

## Contributing

 * See the [Scala Developer Guidelines](https://github.com/scala/scala/blob/2.13.x/CONTRIBUTING.md) for general contributing guidelines
 * Have a look at [existing issues](https://github.com/scala/scala-parser-combinators/issues)
 * Ask questions and discuss [on Gitter](https://gitter.im/scala/scala-parser-combinators)
