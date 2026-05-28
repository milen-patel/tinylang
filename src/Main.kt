fun main(args: Array<String>) {
    //val source : String = "6 + (4*2)/5 - 3";
    //val source : String = "+++++";
    val source: String = "6         +4"
    val tokens: List<Token> = Lexer(source).scanTokens();
    tokens.forEach { token: Token -> println(token)}
}
