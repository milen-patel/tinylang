sealed class Stmt {
    data class VarDeclaration(val name: String, val initializer: Expr): Stmt()
    data class ExpressionStmt(val expression: Expr): Stmt()
    data class VarUpdate(val name: String, val value: Expr): Stmt()
    data class IfStmt(val condition: Expr, val body: List<Stmt>): Stmt()
    data class FunctionDeclaration(val name: String, val parameters: List<String>, val body: List<Stmt>): Stmt()
    data class ReturnStmt(val value: Expr): Stmt()
}
