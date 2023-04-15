package moe.nea.sky.platform.core

import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

class ModuleStreamHandler(val modules: Map<String, ModuleInfo>) : URLStreamHandler() {
    override fun openConnection(u: URL): URLConnection {
        if (u.protocol != "skyneamoe") throw IOException("Invalid protocol name ${u.protocol}")
        val module = modules[u.host] ?: throw IOException("Invalid module name ${u.host}")
        return ModuleConnection(u, module, u.path)
    }

    class ModuleConnection(url: URL, val module: ModuleInfo, val path: String) : URLConnection(url) {
        override fun connect() {
            connected = true
        }

        override fun getInputStream(): InputStream {
            return module.getInputStream(path)
        }
    }
}
