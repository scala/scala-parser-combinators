# scala-parser-combinators

[<img src="https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-parser-combinators_2.12.svg?label=latest%20release%20for%202.12"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.scala-lang.modules%20a%3Ascala-parser-combinators_2.12)
[<img src="https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-parser-combinators_2.13.svg?label=latest%20release%20for%202.13"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.scala-lang.modules%20a%3Ascala-parser-combinators_2.13)
[<img src="https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-parser-combinators_3.svg?label=latest%20release%20for%203"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.scala-lang.modules%20a%3Ascala-parser-combinators_3)

This was originally part of the Scala standard library, but is now community-maintained, under the guidance of the Scala team at Lightbend. If you are interested in joining the maintainers team, please contact [@Philippus](https://github.com/philippus) or [@SethTisue](https://github.com/SethTisue).

## Choosing a parsing library

This library's main strengths are:

* Stability. It's been around and in wide use for more than a decade.
* The codebase is modest in size and its internals are fairly simple.
* It's plain vanilla Scala. No macros, code generation, or other magic is involved.
* All versions of Scala (2.11, 2.12, 2.13, 3) are supported on all back ends (JVM, JS, Native).

Its main weaknesses are:

* Performance. If you are ingesting large amounts of data, you may want something faster.
* Minimal feature set.
* Inflexible, unstructured error reporting.

A number of other parsing libraries for Scala are available -- [see list on Scaladex](https://index.scala-lang.org/awesome/parsing?sort=stars).

## Documentation

 * [Current API](https://javadoc.io/page/org.scala-lang.modules/scala-parser-combinators_2.13/latest/scala/util/parsing/combinator/index.html)
 * The [Getting Started](docs/Getting_Started.md) guide
 * A more complicated example, [Building a lexer and parser with Scala's Parser Combinators](https://enear.github.io/2016/03/31/parser-combinators/)
 * "Combinator Parsing", chapter 33 of [_Programming in Scala, Third Edition_](http://www.artima.com/shop/programming_in_scala), shows how to apply this library to e.g. parsing of arithmetic expressions. The second half of the chapter examines how the library is implemented.

## Adding an sbt dependency

To depend on scala-parser-combinators in sbt, add something like this to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % <version>
```

To support multiple Scala versions, see the example in [scala/scala-module-dependency-sample](https://github.com/scala/scala-module-dependency-sample).

### Scala.js and Scala Native

Scala-parser-combinators is also available for Scala.js and Scala Native:

```
libraryDependencies += "org.scala-lang.modules" %%% "scala-parser-combinators" % <version>
```

## Example

```scala
import scala.util.parsing.combinator._

case class WordFreq(word: String, count: Int) {
  override def toString = s"Word <$word> occurs with frequency $count"
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
      case Failure(msg,_) => println(s"FAILURE: $msg")
      case Error(msg,_) => println(s"ERROR: $msg")
    }
  }
}
```

For a detailed unpacking of this example see
[Getting Started](docs/Getting_Started.md).

## Contributing

 * See the [Scala Developer Guidelines](https://github.com/scala/scala/blob/2.13.x/CONTRIBUTING.md) for general contributing guidelines
 * Have a look at [existing issues](https://github.com/scala/scala-parser-combinators/issues)
 * Ask questions and discuss [in GitHub Discussions](https://github.com/scala/scala-parser-combinators/discussions)
 * Feel free to open draft pull requests with partially completed changes, to get feedback.
