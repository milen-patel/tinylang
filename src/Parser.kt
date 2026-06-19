/*
 * cexpression -> expression (("<" | ">") expression)?
 * expression -> term ('+' | '-' term)*
 * term -> factor ('*' | '/' factor)*
 * factor -> integer | '(' expression ')' | identifier | invocation
 *
 * invocation -> "invoke" identifier "(" params? ")"
 * params -> expression ("," expression)*
 *
 * program -> statement* 
 * statement ->  variableDeclarationStatement | 
 *                expressionStatement | 
 *                variableUpdateStatement | 
 *                ifStatement
 *                functionDeclarationStatement
 *                returnStatement 
 * returnStatement -> "return" cexpression ";"
 * functionDeclarationStatement -> "fun" identifier "(" args? ")" "{" statement* "}"
 * variableDeclarationStatement -> "let" identifier "=" expression ";" 
 * expressionStatement ->               cexpression ";"
 * variableUpdateStatement ->                "update" identifier "to" expression ";" 
 * ifStatement ->"if" "(" cexpression ")" "{" statement* "}" ";"
 * args -> identifier ("," identifier)*
 *
 * identifier -> [a-z][a-z]*
 *
 * Start symbol = statement
 */ 

class Parser(private val tokens: List<Token>, private val shouldLog: Boolean) {
  private var currentPosition: Int = 0

  fun log(stmt: String) {
    if (shouldLog) {
      println(stmt)
    }
  }

  fun parse(): List<Stmt> {
    log("parse() Top level public parse function called")
    val statements: MutableList<Stmt> = mutableListOf()
    while (!isAtEnd()) {
      statements.add(parseTopLevelStatement())
    }

    log("parse() done parsing expression, verifying EOF exists")
    consume(TokenType.END_OF_FILE, "Expected EOF to terminate the program")

    return statements
  }

  private fun parseTopLevelStatement() :Stmt {
    if (match(TokenType.FUN)) {
      return parseFunctionDeclaration()
    }
    return parseStatement()
  }
  
  private fun parseFunctionDeclaration(): Stmt {
    val name: String = consume(TokenType.IDENTIFIER, "Function needs a name").literal
    consume(TokenType.OPEN_PARENTHESIS, "Expected ( in function definition")
    val parameters: MutableList<String> = mutableListOf()
    while (!check(TokenType.CLOSE_PARENTHESIS)) {
      parameters.add(consume(TokenType.IDENTIFIER, "Expected identifier").literal)
      while (match(TokenType.COMMA)) {
        parameters.add(consume(TokenType.IDENTIFIER, "Expected identifier").literal)
      }
    }
    consume(TokenType.CLOSE_PARENTHESIS, "Expected ) in function definition")
    consume(TokenType.OPEN_BRACE, "Expected {")
    val body: MutableList<Stmt> = mutableListOf()
    while(!check(TokenType.CLOSE_BRACE) && !isAtEnd()) {
      body.add(parseStatement())
    }
    consume(TokenType.CLOSE_BRACE, "Expected }")
    return Stmt.FunctionDeclaration(name, parameters, body)
  }

  private fun parseReturnStatement(): Stmt {
      val value : Expr = parseCExpression()
      consume(TokenType.SEMICOLON, "Expected ;")
      return Stmt.ReturnStmt(value)
  }
  
  private fun parseStatement(): Stmt {
    if (match(TokenType.RETURN)) {
      return parseReturnStatement()
    }
    if (match(TokenType.LET)) {
      return parseVarDeclaration()
    }
    if (match(TokenType.UPDATE)) {
        return parseVarUpdate()
    }
    if (match(TokenType.IF)) {
      return parseIfStatement()
    }
    return parseExpressionStatment()
  }

  private fun parseIfStatement(): Stmt {
    consume(TokenType.OPEN_PARENTHESIS, "Expected ( after if")
    val condition: Expr = parseCExpression()
    consume(TokenType.CLOSE_PARENTHESIS, "Expected ) after if")
    consume(TokenType.OPEN_BRACE, "Expected { after )")
    val body: MutableList<Stmt> = mutableListOf()
    while (!check(TokenType.CLOSE_BRACE) && !isAtEnd()) {
      body.add(parseStatement())
    }
    consume(TokenType.CLOSE_BRACE, "Expected } to end if statement body")
    consume(TokenType.SEMICOLON, "Expected ; to end if statement")
    return Stmt.IfStmt(condition, body)
  }
 

  private fun parseVarUpdate(): Stmt {
      val name: String = consume(TokenType.IDENTIFIER, "must specify variable name to update").literal
      consume(TokenType.TO, "Expected literal 'to'")
      val value: Expr = parseExpression()
      consume(TokenType.SEMICOLON, "Expected semicolon to end statement")
      return Stmt.VarUpdate(name, value)
  }

  private fun parseVarDeclaration(): Stmt {
    val name: String = consume(TokenType.IDENTIFIER, "Expect a name for a variable declaration").literal
    consume(TokenType.EQUAL, "Expected = sign after variable name")
    val initializer: Expr = parseExpression()
    consume(TokenType.SEMICOLON, "Expected ; following variable declaration")
    return Stmt.VarDeclaration(name, initializer)
  }
  private fun parseExpressionStatment(): Stmt {
    val expression: Expr = parseCExpression()
    consume(TokenType.SEMICOLON, "Expected ; following expression statement")
    return Stmt.ExpressionStmt(expression)
  }

  private fun parseCExpression(): Expr {
    var workingExpression: Expr = parseExpression()
    if (match(TokenType.LESS_THAN, TokenType.GREATER_THAN)) {
      val operator: Token = previous()
      val parsedExpression: Expr = parseExpression()
      workingExpression = Expr.Binary(workingExpression, operator, parsedExpression)
    }
    return workingExpression
  }

  private fun parseExpression(): Expr {
    log("parseExpression() called, will start by parsing term()")
    var  workingExpression: Expr = parseTerm()
    log("parseExpression() done parsing term, now checking for + - Term")
    while (match(TokenType.PLUS, TokenType.MINUS)) {
      log("parseExpression() found a ${previous()}, will parse term again")
      val operator: Token = previous()
      val parsedTerm: Expr = parseTerm()
      // Left unfolding that maintains left-to-right execution of terms of the same precedence
      workingExpression = Expr.Binary(workingExpression, operator, parsedTerm)
    }
    return workingExpression

  }

  private fun parseTerm(): Expr {
    log("parseTerm() called, will start by calling parseFactor()")
    var workingExpression: Expr = parseFactor()

    log("parseTerm() done parsing term, now checking for * / Term")
    while (match(TokenType.STAR, TokenType.SLASH)) {
        log("parseTerm() found a ${previous()}, will parse term again")
        val operator: Token = previous()
        val parsedFactor: Expr = parseFactor()
        workingExpression = Expr.Binary(workingExpression, operator, parsedFactor)
    }
    return workingExpression
  }

  private fun parseFactor(): Expr {
    log("parseFactor() called")
    // Case 1: integer
    if (match(TokenType.NUMBER)) {
      log("parseFactor() matched a ${previous()} , will return literal")
      return Expr.NumberLiteral(previous().literal.toInt())
    }

    // Case 2: ( E )
    if (match(TokenType.OPEN_PARENTHESIS)) {
      log("parseFactor() matched an open parenthesis, will parse expression now")
      val parsedExpression: Expr = parseExpression()
      log("parseFactor() done parsing expression, checking for close parenthesis")
      consume(TokenType.CLOSE_PARENTHESIS, "Failed to parse a factor, expected ) following (") 
      return parsedExpression
    }

    // Case 3: identifier
    if (match(TokenType.IDENTIFIER)) {
      return Expr.Variable(previous().literal)
    }

    // Case 4: Function invocation
    if (match(TokenType.INVOKE)) {
      val name: String = consume(TokenType.IDENTIFIER, "Expected identifier").literal
      consume(TokenType.OPEN_PARENTHESIS, "Expected (")
      val args: MutableList<Expr> = mutableListOf()
      while(!check(TokenType.CLOSE_PARENTHESIS)) {
        args.add(parseCExpression())
        while (match(TokenType.COMMA)) {
          args.add(parseCExpression())
        }
      }
      consume(TokenType.CLOSE_PARENTHESIS, "Expected )")
      return Expr.FunctionCall(name, args)
    }

    error("Unable to parse factor, expected a number or open parenthesis")
  }

  private fun peek(): Token {
    return tokens[currentPosition];
  }

  private fun isAtEnd(): Boolean {
    return peek().type == TokenType.END_OF_FILE
  }

  private fun previous(): Token {
    return tokens[currentPosition - 1]
  }

  private fun advance(): Token {
    if (!isAtEnd()) {
      currentPosition = currentPosition + 1
    }
    return previous()
  }

  private fun check(type: TokenType): Boolean {
    if (isAtEnd()) {
      return type == TokenType.END_OF_FILE
    }
    return peek().type == type
  }

  private fun consume(type: TokenType, message: String): Token {
    if (check(type)) {
        return advance()
    }
    error(message)
  }
  
  private fun match(vararg types: TokenType): Boolean {
    for (type in types) {
        if (check(type)) {
            advance()
            return true
        }
    }
    return false
 }
 



}
