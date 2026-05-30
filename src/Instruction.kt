sealed class Instruction {
    data class PushInt(val value: Int): Instruction()
    object Add : Instruction()
    object Sub : Instruction()
    object Mul : Instruction()
    object Div : Instruction()
}
