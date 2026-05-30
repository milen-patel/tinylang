/*
 * E -> T ('+' | '-' T)*
 * T -> F ('*' | '/' F)*
 * F -> integer | '(' E ')'
 */ 

class Parser(private val tokens: List<Token>) {
  private val enableTracing: Boolean = false
  private var currentPosition: Int = 0

  fun log(stmt: String) {
    if (enableTracing) {
      println(stmt)
    }
  }

  fun parse(): Expr {
    log("parse() Top level public parse function called")
    val expression: Expr = parseExpression()

    log("parse() done parsing expression, verifying EOF exists")
    consume(TokenType.END_OF_FILE, "Expected EOF to terminate the program")

    return expression
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
