package moe.nea.sky.platform.core.render

interface MatrixStack {
    fun pushMatrix()
    fun popMatrix()
    fun translate(x: Double, y: Double, z: Double)
    fun scale(x: Double, y: Double, z: Double)
    fun rotate(angle: Float, x: Double, y: Double, z: Double)
}
