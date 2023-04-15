package moe.nea.sky.platform.core

import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

data class ModuleInfo(
    val name: String,
    val location: Path,
    /* Note: altLocation may not be an archive, and must be a directory structure */
    val altLocation: Path?,
    val identifier: String,
    val dependencies: List<String>
) {
    private var fileSystem = if (location.isRegularFile()) {
        FileSystems.newFileSystem(location.toUri(), mapOf<String, Any?>())
    } else null
    private val basePath = fileSystem?.getPath("/") ?: location

    fun destroy() {
        fileSystem?.close()
    }

    fun getInputStream(path: String): InputStream {
        var pathObj = basePath.resolve(path)
        if (!pathObj.exists() && altLocation != null) {
            pathObj = altLocation.resolve(path)
        }
        return Files.newInputStream(pathObj)
    }
}
