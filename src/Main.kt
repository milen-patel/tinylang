fun main(args: Array<String>) {
    //val source : String = "6 + (4*2)/5 - 3*(4+(4*4 +5))";
    //val source : String = "+++++";
    //val source: String = "1+2+3+4"
    val source: String = "let x = 5*2; update x to x * 2; update x to x + 1; x;"
    println("Source = $source")
    println("====")

    val tokens: List<Token> = Lexer(source).scanTokens();
    // tokens.forEach { token: Token -> println(token)}

    val program: List<Stmt> = Parser(tokens, shouldLog = false).parse()
    program.forEach { currStatement: Stmt -> prettyPrint(currStatement, 0)}


    val instructions: List<Instruction> = Compiler(shouldLog = false).compile(program)
    println("=========")
    instructions.forEach { instruction: Instruction -> println(instruction) }

    val finalStack: List<Int> = Machine().run(instructions)
    println("=========")
    println("Final stack = $finalStack")
}

fun prettyPrint(stmt: Stmt, indent: Int) { 
  val padding: String = "    ".repeat(indent)
  when (stmt) {
    is Stmt.VarDeclaration -> {
      println("${padding}VarDeclaration named ${stmt.name}")
      prettyPrint(stmt.initializer, indent + 1)

    }
    is Stmt.ExpressionStmt -> {
        println("${padding}ExpressionStmt")
        prettyPrint(stmt.expression, indent + 1)
    }
    is Stmt.VarUpdate -> {
        println("${padding}VarUpdate name ${stmt.name}")
        prettyPrint(stmt.value, indent + 1)
    }
  }
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
    is Expr.Variable -> {
      println("${padding}Variable named ${expr.name}")
    }
  }
}
