sealed class Instruction {
    data class PushInt(val value: Int): Instruction()
    object Add : Instruction() {
        override fun toString(): String = "Add"
    }
    object Sub : Instruction() {
        override fun toString(): String = "Sub"
    }
    object Mul : Instruction() {
        override fun toString(): String = "Mul"
    }
    object Div : Instruction() {
        override fun toString(): String = "Div"
    }
    data class LoadLocal(val slot: Int): Instruction()
    data class StoreLocal(val slot: Int): Instruction()
}
