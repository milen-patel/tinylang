sealed class Expr {
  data class NumberLiteral(val value: Int): Expr()
  data class Binary(
    val left: Expr,
    val operator: Token, 
    val right: Expr,
  ) : Expr()
}
