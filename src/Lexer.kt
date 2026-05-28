class Lexer(val input: String) {

  private val tokens: MutableList<Token> = mutableListOf();
  private var currentPosition: Int = 0;

  private fun advance(): Char { 
    val returnCharacter: Char = input[currentPosition];
    currentPosition = currentPosition + 1;
    return returnCharacter;
  }
  
  private fun isAtEnd(): Boolean {
    return currentPosition >= input.length;
  }

  fun scanTokens(): List<Token> {
      while (!isAtEnd()) {
        scanNextToken()
      }
      return tokens;
  }

  private fun peek(): Char {
      if (isAtEnd()) {
        return 'd';
      }
      return input[currentPosition];
  }

  private fun scanNumber() {
    val startingPositionOfNumber: Int = currentPosition - 1;
    while (peek().isDigit()) {
      advance();
    }
    tokens.add(Token(TokenType.NUMBER, input.substring(startingPositionOfNumber, currentPosition)))
  }

  private fun scanNextToken() {
    val currentCharacter: Char = advance()
    if (currentCharacter == '(') {
      tokens.add(Token(TokenType.OPEN_PARENTHESIS))
    } else if (currentCharacter == ')') {
      tokens.add(Token(TokenType.CLOSE_PARENTHESIS))
    } else if (currentCharacter == '+') {
      tokens.add(Token(TokenType.PLUS))
    } else if (currentCharacter == '-') {
      tokens.add(Token(TokenType.MINUS))
    } else if (currentCharacter == '*') {
      tokens.add(Token(TokenType.STAR))
    } else if (currentCharacter == '/') {
      tokens.add(Token(TokenType.SLASH))
    } else if (currentCharacter.isDigit()) {
      scanNumber()
    } else if (currentCharacter == ' ' || currentCharacter == '\n' || currentCharacter == '\t' || currentCharacter == '\r') {
      // Ignore white space
    } else {
      error("Unexpected token: $currentCharacter")
    }
  }
}
