fun main(args: Array<String>) {
    val source : String = "6 + (4*2)/5 - 3";
    //val source : String = "+++++";
    //val source: String = "1+2+3+4"
    val tokens: List<Token> = Lexer(source).scanTokens();
    tokens.forEach { token: Token -> println(token)}

    println("Source = $source")
    println("====")
    val expression: Expr = Parser(tokens).parse()
    prettyPrint(expression,0)
}

fun prettyPrint(expr: Expr, indent: Int) {
  val padding: String = "    ".repeat(indent)
  when (expr) {
    is Expr.NumberLiteral -> {
      println("${padding}Number(${expr.value})")
    }
    is Expr.Binary -> {
      println("${padding}Binary(${expr.operator.type})")
      prettyPrint(expr.left, indent + 1)
      prettyPrint(expr.right, indent + 1)
    }
  }
}
