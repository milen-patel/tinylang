class Machine {
  private val stack: MutableList<Int> = mutableListOf()
  private val locals: MutableList<Int?> = mutableListOf()

  fun run(instructions: List<Instruction>): List<Int> {
    for (instruction in instructions) {
      execute(instruction)
    }
    return stack.toList()
  }

  private fun execute(instruction: Instruction) {
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
      is Instruction.StoreLocal -> storeLocal(instruction.slot, pop())
      is Instruction.LoadLocal -> stack.add(loadLocal(instruction.slot))
    }
  }

  private fun pop(): Int {
    if (stack.isEmpty()) {
      error("Stack underflow")
    }
    return stack.removeAt(stack.lastIndex)
  }

  private fun storeLocal(slot: Int, value: Int) {
    while (locals.size <= slot) {
      locals.add(null)
    }
    locals[slot] = value
  }

  private fun loadLocal(slot: Int): Int {
    if (slot >= locals.size || locals[slot] == null) {
      error("Undefined local slot $slot")
    }
    return locals[slot]!!
  }
}
