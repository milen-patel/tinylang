enum class TokenType {
  NUMBER,
  PLUS,
  MINUS,
  STAR,
  SLASH,
  OPEN_PARENTHESIS,
  CLOSE_PARENTHESIS,
  END_OF_FILE,
  LET,
  IDENTIFIER,
  EQUAL,
  SEMICOLON,
  UPDATE,
  TO,
  LESS_THAN,
  GREATER_THAN,
  OPEN_BRACE,
  CLOSE_BRACE,
  IF,
  FUN,
  COMMA,
  RETURN,
  INVOKE
}

data class Token(
  val type: TokenType,
  val literal: String = "",
)
