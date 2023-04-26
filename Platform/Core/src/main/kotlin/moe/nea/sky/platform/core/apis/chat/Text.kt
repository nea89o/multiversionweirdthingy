package moe.nea.sky.platform.core.apis.chat

sealed class Text {
    var style: TextStyle? = null
    val children: MutableList<Text> = mutableListOf()
    class Literal(val text: String) : Text()

}