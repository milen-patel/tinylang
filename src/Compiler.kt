class Compiler(val shouldLog: Boolean = true) {

  private data class LocalContext(
    val locals: MutableMap<String, Int>  = mutableMapOf(),
    var nextLocalSlot: Int = 0,
    var isFunctionBody: Boolean = false,
  )

  private data class FunctionSignature(
    val parameters: List<String>,
    var address: Int = -1,
  )


  private data class PendingFunctionCall(
    val name: String,
    val instructionIndex: Int, 
    val arity: Int,
  )

  private val functionSignatures: MutableMap<String, FunctionSignature> = mutableMapOf()
  private val pendingFunctionCalls: MutableList<PendingFunctionCall> = mutableListOf()


  fun log(stmt: String) {
    if (shouldLog) {
      println(stmt)
    }
  }

  fun compile(statements: List<Stmt>): CompileResult {
    log("Top level compile function called")
    val mainContext = LocalContext()
    val instructions: MutableList<Instruction> = mutableListOf()
    instructions.add(Instruction.Jump(999))
    // Register function signature
    statements.forEach { statement: Stmt ->
      if (statement is Stmt.FunctionDeclaration) {
        registerSignature(statement)
      }
    }

    // Parse function bodies
    statements.forEach { statement: Stmt ->
      if (statement is Stmt.FunctionDeclaration) {
        setFunctionAddress(statement.name, instructions.size)
        emitFunctionDeclaration(statement, instructions)
      }
    }

    instructions[0] = Instruction.Jump(instructions.size)
    statements.forEach { statement: Stmt -> 
      if (statement !is Stmt.FunctionDeclaration) {
        emit(statement, instructions, mainContext) 
      }
    }
    patchFunctionCalls(instructions)
    return CompileResult(instructions)
  }

  private fun patchFunctionCalls(instructions: MutableList<Instruction>) {
    pendingFunctionCalls.forEach { pendingCall: PendingFunctionCall ->
      val signature: FunctionSignature = functionSignatures[pendingCall.name] ?: error("Unreachable")
      instructions[pendingCall.instructionIndex] = Instruction.CallFunction(signature.address, pendingCall.arity)
    }
  }

  private fun setFunctionAddress(name: String, address: Int) {
    val signature: FunctionSignature = functionSignatures[name] ?:  error("shouldnt be possible")
    signature.address = address
  }

  private fun emitFunctionDeclaration(stmt: Stmt.FunctionDeclaration, instructions: MutableList<Instruction>) {
    if (stmt.body.lastOrNull() !is Stmt.ReturnStmt) {
      error("Functions need to end with a return statement.")
    }

      val functionContext = LocalContext(isFunctionBody = true)
      stmt.parameters.forEach { parameter: String ->
          if (functionContext.locals.containsKey(parameter)) {
            error("Duplicate param defeinition")
          }
          val slot: Int = functionContext.nextLocalSlot
          functionContext.nextLocalSlot = functionContext.nextLocalSlot + 1
          functionContext.locals[parameter] = slot
      }
      stmt.body.forEach { bodyStatement: Stmt -> 
        emit(bodyStatement, instructions, functionContext)
      }
  }

  private fun registerSignature(stmt: Stmt.FunctionDeclaration) {
    if (functionSignatures.containsKey(stmt.name)) {
      error("Duplicate function declaration")
    }
    val seenParameters: MutableSet<String> = mutableSetOf()
    stmt.parameters.forEach { parameter: String ->
      if (!seenParameters.add(parameter)) {
        error("Duplicate param definition in function")
      }
    }
    functionSignatures[stmt.name] = FunctionSignature(stmt.parameters)
  }


  private var nextUniqueNumber: Int = 1

  private fun emit(stmt: Stmt, instructions: MutableList<Instruction>, context: LocalContext) {
    when (stmt) {
      is Stmt.ExpressionStmt -> {
          emit(stmt.expression, instructions, context)
      }
      is Stmt.VarDeclaration -> {
          if (context.locals.containsKey(stmt.name)) {
            error("Duplicate definition of variable detected ${stmt.name}")
          }
          emit(stmt.initializer, instructions, context)
          val slot: Int = context.nextLocalSlot
          context.nextLocalSlot = context.nextLocalSlot + 1
          context.locals[stmt.name] = slot
          instructions.add(Instruction.StoreLocal(slot))
      }
      is Stmt.VarUpdate -> {
          val slot: Int? = context.locals[stmt.name]
          if (slot == null) {
            error("Failed to update a variable with ${stmt.name} as name before declaration")
          }
          emit(stmt.value, instructions, context)
          instructions.add(Instruction.StoreLocal(slot))
      }
    /*
     * instructions before the if statement
     * instructions nededed to evaluate the condition of the if statement
     * jump if false instruction 
     * instructions for each statement in the body of the if-statement {..}
     * target jump location
     */
      is Stmt.IfStmt -> {
        emit(stmt.condition, instructions, context)
        val jumpInstructionIndex: Int = instructions.size
        instructions.add(Instruction.JumpIfFalse(999))
        stmt.body.forEach { bodyStatement: Stmt -> emit(bodyStatement, instructions, context) }
        val realJumpLocation: Int = instructions.size
        instructions[jumpInstructionIndex] = Instruction.JumpIfFalse(realJumpLocation)
      }
      is Stmt.FunctionDeclaration -> {
        error("Functions can only be declared at the top level")
      }
      is Stmt.ReturnStmt -> {
        if (!context.isFunctionBody) {
          error("Return statements may only appear within the body of a function")
        }
        emit(stmt.value, instructions, context)
        instructions.add(Instruction.Return)
      }
    }


  }
  private fun emit(expr: Expr, instructions: MutableList<Instruction>, context: LocalContext) {
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
          emit(expr.left, instructions, context)

          log("[$logId] Recursing on the right side")
          emit(expr.right, instructions, context)
          
          log("[$logId] Now adding binary operator ${instructionForOperator(expr.operator)}")
          instructions.add(instructionForOperator(expr.operator))

      }
      is Expr.Variable -> {
        val slot: Int? = context.locals[expr.name]
        if (slot == null) {
          error("Referencing undefined variable ${expr.name}")
        }
        instructions.add(Instruction.LoadLocal(slot))
      }
      is Expr.FunctionCall -> {
        val signature: FunctionSignature = functionSignatures[expr.name] ?: error("Calling unknown function")
        if (signature.parameters.size != expr.arguments.size) {
          error("Function expected a different number of args than it received")
        }
        expr.arguments.forEach { argument: Expr -> 
          emit(argument, instructions, context)
        }
        if (signature.address > 0) {
          instructions.add(Instruction.CallFunction(signature.address, expr.arguments.size))
        } else {
          pendingFunctionCalls.add(PendingFunctionCall(expr.name, instructions.size, expr.arguments.size))
          instructions.add(Instruction.CallFunction(999, 999))
        }
      }
    }

  }

  private fun instructionForOperator(operator: Token): Instruction {
    return when (operator.type) {
        TokenType.PLUS -> Instruction.Add
        TokenType.MINUS -> Instruction.Sub
        TokenType.STAR -> Instruction.Mul
        TokenType.SLASH -> Instruction.Div
        TokenType.LESS_THAN -> Instruction.LessThan
        TokenType.GREATER_THAN -> Instruction.GreaterThan
        else -> error("Invalid operator $operator")
    }
  }
}
