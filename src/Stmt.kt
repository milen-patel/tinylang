sealed class Stmt {
    data class VarDeclaration(val name: String, val initializer: Expr): Stmt()
    data class ExpressionStmt(val expression: Expr): Stmt()
}
