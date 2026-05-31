class Compiler(val shouldLog: Boolean = true) {
  private val locals: MutableMap<String, Int>  = mutableMapOf()
  private var nextLocalSlot: Int = 0

  fun log(stmt: String) {
    if (shouldLog) {
      println(stmt)
    }
  }

  fun compile(statements: List<Stmt>): List<Instruction> {
    log("Top level compile function called")
    val instructions: MutableList<Instruction> = mutableListOf()
    statements.forEach { statement: Stmt -> emit(statement, instructions) }
    return instructions
  }


  private var nextUniqueNumber: Int = 1

  private fun emit(stmt: Stmt, instructions: MutableList<Instruction>) {
    when (stmt) {
      is Stmt.ExpressionStmt -> {
          emit(stmt.expression, instructions)
      }
      is Stmt.VarDeclaration -> {
          if (locals.containsKey(stmt.name)) {
            error("Duplicate definition of variable detected ${stmt.name}")
          }
          emit(stmt.initializer, instructions)
          val slot: Int = nextLocalSlot
          nextLocalSlot = nextLocalSlot + 1
          locals[stmt.name] = slot
          instructions.add(Instruction.StoreLocal(slot))
      }
    }


  }
  private fun emit(expr: Expr, instructions: MutableList<Instruction>) {
    val logId: Int = nextUniqueNumber
    nextUniqueNumber = nextUniqueNumber + 1
    when (expr) {
      is Expr.NumberLiteral -> {
          log("[$logId] Emit called with number literal ${expr.value}")
          instructions.add(Instruction.PushInt(expr.value))
      }
      is Expr.Binary -> {
          log("[$logId] Emit called with binary expression ${expr}")
          log("[$logId] Recursing on the left side")
          emit(expr.left, instructions)

          log("[$logId] Recursing on the right side")
          emit(expr.right, instructions)
          
          log("[$logId] Now adding binary operator ${instructionForOperator(expr.operator)}")
          instructions.add(instructionForOperator(expr.operator))

      }
      is Expr.Variable -> {
        val slot: Int? = locals[expr.name]
        if (slot == null) {
          error("Referencing undefined variable ${expr.name}")
        }
        instructions.add(Instruction.LoadLocal(slot))
      }
    }

  }

  private fun instructionForOperator(operator: Token): Instruction {
    return when (operator.type) {
        TokenType.PLUS -> Instruction.Add
        TokenType.MINUS -> Instruction.Sub
        TokenType.STAR -> Instruction.Mul
        TokenType.SLASH -> Instruction.Div
        else -> error("Invalid operator $operator")
    }
  }
}
