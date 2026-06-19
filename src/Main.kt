fun main(args: Array<String>) {
    //val source : String = "6 + (4*2)/5 - 3*(4+(4*4 +5))";
    //val source : String = "+++++";
    //val source: String = "1+2+3+4"
    //val source: String = "let x = 5*2; update x to x * 2; update x to x + 1; x;"
    //val source: String = "let x = 9; if (x < 5) { update x to x + 10; let y = 5; }; x+y;"
    val source: String = """
    fun bump(n) {
      let x = n + 1;
      if (x > 5) {
        update x to x * 2;
      };
      return x;
    }
    let a = invoke bump(5);
    let b = invoke bump(a);  
    update b to b + invoke bump(1);
    if (b > 20) {
      update b to b - a;
    };
    b;
    """
    println("Source = $source")
    println("====")

    val tokens: List<Token> = Lexer(source).scanTokens();
    // tokens.forEach { token: Token -> println(token)}

    val program: List<Stmt> = Parser(tokens, shouldLog = false).parse()
    program.forEach { currStatement: Stmt -> prettyPrint(currStatement, 0)}


    val result: CompileResult = Compiler(shouldLog = false).compile(program)
    println("=========")
    result.instructions.forEachIndexed { index: Int, instruction: Instruction  -> println("[$index]$instruction") }

    val finalStack: List<Int> = Machine().run(result.instructions)
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
    is Stmt.IfStmt -> {
      println("${padding} If statement")
      println("${padding} Condition")
      prettyPrint(stmt.condition, indent + 2)
      println("${padding} Body")
      stmt.body.forEach { bodyItem: Stmt -> prettyPrint(bodyItem, indent + 2) }
    }
    is Stmt.FunctionDeclaration -> {
      println("${padding}FunctionDeclaration named ${stmt.name}")
      println("${padding}Parameters:")
      stmt.parameters.forEach { p: String -> println("${padding} $p")}
      println("${padding}Body:")
      stmt.body.forEach {item: Stmt -> prettyPrint(item, indent + 2)}
    }
    is Stmt.ReturnStmt -> {
      println("${padding}ReturnStmt")
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
    is Expr.FunctionCall -> {
      println("${padding} FunctionCall to ${expr.name}")
      println("${padding} Args:")
      expr.arguments.forEach { currArg: Expr -> prettyPrint(currArg, indent + 2) }
    }
  }
}
