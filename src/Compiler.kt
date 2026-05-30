class Compiler(val shouldLog: Boolean = true) {

  fun log(stmt: String) {
    if (shouldLog) {
      println(stmt)
    }
  }

  fun compile(expr: Expr): List<Instruction> {
    log("Top level compile function called")
    val instructions: MutableList<Instruction> = mutableListOf()
    emit(expr, instructions)
    return instructions
  }


  private var nextUniqueNumber: Int = 1
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
