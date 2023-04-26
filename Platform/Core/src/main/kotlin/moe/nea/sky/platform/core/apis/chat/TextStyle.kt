package moe.nea.sky.platform.core.apis.chat

data class TextStyle(
    val color: TextColor? = null,
    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val obfuscated: Boolean? = null,
    val strikethrough: Boolean? = null,
    val underlined: Boolean? = null,
) {
    /*
    * Missing attributes:
    *  - Hover data
    *  - Click data
    * */
}
