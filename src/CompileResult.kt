  data class CompileResult(
    val instructions: List<Instruction>,
    val functions: Map<String, FunctionDefinition>,
  )
