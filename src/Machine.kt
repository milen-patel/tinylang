class Machine {
  private val stack: MutableList<Int> = mutableListOf()
  private val locals: MutableList<Int?> = mutableListOf()
  private val functions: MutableMap<String, FunctionDefinition> = mutableMapOf()

  fun run(instructions: List<Instruction>, functionDefinitions: Map<String, FunctionDefinition> = emptyMap()): List<Int> {
    functions.clear()
    functions.putAll(functionDefinitions)
    return runWithLocals(instructions, locals)
  }

  private fun runWithLocals(instructions: List<Instruction>, activeLocals: MutableList<Int?>): List<Int> {
    var instructionPointer: Int = 0
    while (instructionPointer < instructions.size) {
      if (instructions[instructionPointer] == Instruction.Return) {
        return stack.toList()
      }
      val nextInstructionPointer: Int? = execute(instructions[instructionPointer], activeLocals)
      instructionPointer = nextInstructionPointer ?: instructionPointer + 1
    }
    return stack.toList()
  }

  private fun execute(instruction: Instruction, activeLocals: MutableList<Int?>): Int? {
    when (instruction) {
      is Instruction.PushInt -> stack.add(instruction.value)
      Instruction.Add -> {
        val right = pop()
        val left = pop()
        stack.add(left + right)
      }
      Instruction.Sub -> {
        val right = pop()
        val left = pop()
        stack.add(left - right)
      }
      Instruction.Mul -> {
        val right = pop()
        val left = pop()
        stack.add(left * right)
      }
      Instruction.Div -> {
        val right = pop()
        val left = pop()
        stack.add(left / right)
      }
      Instruction.LessThan -> {
        val right = pop()
        val left = pop()
        stack.add(if (left < right) 1 else 0)
      }
      Instruction.GreaterThan -> {
        val right = pop()
        val left = pop()
        stack.add(if (left > right) 1 else 0)
      }
      is Instruction.StoreLocal -> storeLocal(activeLocals, instruction.slot, pop())
      is Instruction.LoadLocal -> stack.add(loadLocal(activeLocals, instruction.slot))
      is Instruction.JumpIfFalse -> {
        val condition = pop()
        if (condition == 0) {
          return instruction.target
        }
      }
      is Instruction.CallFunction -> callFunction(instruction)
      Instruction.Return -> error("Return instruction should be handled by the instruction loop")
    }
    return null
  }

  private fun pop(): Int {
    if (stack.isEmpty()) {
      error("Stack underflow")
    }
    return stack.removeAt(stack.lastIndex)
  }

  private fun storeLocal(activeLocals: MutableList<Int?>, slot: Int, value: Int) {
    while (activeLocals.size <= slot) {
      activeLocals.add(null)
    }
    activeLocals[slot] = value
  }

  private fun loadLocal(activeLocals: MutableList<Int?>, slot: Int): Int {
    if (slot >= activeLocals.size || activeLocals[slot] == null) {
      error("Undefined local slot $slot")
    }
    return activeLocals[slot]!!
  }

  private fun callFunction(instruction: Instruction.CallFunction) {
    val function: FunctionDefinition = functions[instruction.name]
      ?: error("Undefined function ${instruction.name}")
    if (function.parameters.size != instruction.arity) {
      error("Function ${instruction.name} expects ${function.parameters.size} arguments but got ${instruction.arity}")
    }

    val callLocals: MutableList<Int?> = mutableListOf()
    val arguments: MutableList<Int> = mutableListOf()
    repeat(instruction.arity) {
      arguments.add(pop())
    }
    arguments.reverse()

    arguments.forEachIndexed { index: Int, argument: Int ->
      storeLocal(callLocals, index, argument)
    }
    runWithLocals(function.instructions, callLocals)
  }
}
