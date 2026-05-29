/*
 * E -> T ('+' | '-' T)*
 * T -> F ('*' | '/' F)*
 * F -> integer | '(' E ')'
 */ 

enum class TokenType {
  NUMBER,
  PLUS,
  MINUS,
  STAR,
  SLASH,
  OPEN_PARENTHESIS,
  CLOSE_PARENTHESIS,
  END_OF_FILE,
}

data class Token(
  val type: TokenType,
  val literal: String = "",
)
