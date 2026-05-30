class Machine {
  private val stack: MutableList<Int> = mutableListOf()

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
    }
  }

  private fun pop(): Int {
    if (stack.isEmpty()) {
      error("Stack underflow")
    }
    return stack.removeAt(stack.lastIndex)
  }
}
