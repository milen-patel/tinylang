/*
 * E -> E + T
 * E -> E - T
 * E -> T 
 *
 * T -> T * F 
 * T -> T / F 
 * T -> F 
 *
 *  F -> integer
 *  F -> ( E )
 */ 

enum class TokenType {
  NUMBER,
  PLUS,
  MINUS,
  STAR,
  SLASH,
  OPEN_PARENTHESIS,
  CLOSE_PARENTHESIS
}

data class Token(
  val type: TokenType,
  val literal: String = "",
)
