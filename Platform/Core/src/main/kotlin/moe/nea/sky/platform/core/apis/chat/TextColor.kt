package moe.nea.sky.platform.core.apis.chat

interface TextColor {
    fun toSimpleColor(): Simple
    fun toRGB(): Int
    enum class Simple(r: Int, g: Int, b: Int) : TextColor {
        BLACK(0, 0, 0),
        DARK_BLUE(0, 0, 170),
        DARK_GREEN(0, 170, 0),
        DARK_AQUA(0, 170, 170),
        DARK_RED(170, 0, 0),
        DARK_PURPLE(170, 0, 170),
        GOLD(255, 170, 0),
        GRAY(170, 170, 170),
        DARK_GRAY(85, 85, 85),
        BLUE(85, 85, 255),
        GREEN(85, 255, 85),
        AQUA(85, 255, 255),
        RED(255, 85, 0),
        LIGHT_PURPLE(255, 85, 255),
        YELLOW(255, 255, 85),
        WHITE(255, 255, 255);

        override fun toRGB(): Int {
            return rgb
        }

        override fun toSimpleColor(): Simple = this
        val metadata: Int = ordinal
        val symbol = metadata.toString(16)
        val rgb = (r shl 16) or (g shl 8) or b
    }
}
