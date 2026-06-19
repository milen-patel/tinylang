class Machine {
  private data class CallFrame(
    val returnAddress: Int,
    val locals: MutableList<Int?>,
  )

  private val stack: MutableList<Int> = mutableListOf()
  private val callStack: MutableList<CallFrame> = mutableListOf()

  fun run(instructions: List<Instruction>): List<Int> {
    stack.clear()
    callStack.clear()
    var activeLocals: MutableList<Int?> = mutableListOf()
    var instructionPointer: Int = 0
    while (instructionPointer < instructions.size) {
      val nextInstructionPointer: Int? = execute(
        instructions[instructionPointer],
        activeLocals,
        instructionPointer + 1,
      ) { restoredLocals: MutableList<Int?> ->
        activeLocals = restoredLocals
      }
      instructionPointer = nextInstructionPointer ?: instructionPointer + 1
    }
    return stack.toList()
  }

  private fun execute(
    instruction: Instruction,
    activeLocals: MutableList<Int?>,
    nextInstructionPointer: Int,
    restoreLocals: (MutableList<Int?>) -> Unit,
  ): Int? {
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
      is Instruction.Jump -> return instruction.target
      is Instruction.JumpIfFalse -> {
        val condition = pop()
        if (condition == 0) {
          return instruction.target
        }
      }
      is Instruction.CallFunction -> {
        val callLocals: MutableList<Int?> = createFunctionLocals(instruction.arity)
        callStack.add(CallFrame(nextInstructionPointer, activeLocals))
        restoreLocals(callLocals)
        return instruction.address
      }
      Instruction.Return -> {
        if (callStack.isEmpty()) {
          return null
        }
        val frame: CallFrame = callStack.removeAt(callStack.lastIndex)
        restoreLocals(frame.locals)
        return frame.returnAddress
      }
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

  private fun createFunctionLocals(arity: Int): MutableList<Int?> {
    val callLocals: MutableList<Int?> = mutableListOf()
    val arguments: MutableList<Int> = mutableListOf()
    repeat(arity) {
      arguments.add(pop())
    }
    arguments.reverse()

    arguments.forEachIndexed { index: Int, argument: Int ->
      storeLocal(callLocals, index, argument)
    }
    return callLocals
  }
}
