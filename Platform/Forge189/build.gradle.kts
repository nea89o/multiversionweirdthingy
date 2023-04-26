plugins {
    idea
    id("xyz.wagyourtail.unimined")
    kotlin("jvm")
}

minecraft {
    forge {
        it.mcpChannel = "stable"
        it.mcpVersion = "22-1.8.9"
        it.setDevFallbackNamespace("intermediary")
    }
    launcher.config("client") {
        this.args.add(0, "--tweakClass")
        this.args.add(1, "net.minecraftforge.fml.common.launcher.FMLTweaker")
    }
}

dependencies {
    minecraft("net.minecraft:minecraft:1.8.9")
    mappings("moe.nea.mcp:mcp-yarn:1.8.9")
    "forge"("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    implementation(project(":Platform:Core"))

}

project.afterEvaluate {
    tasks.named("runClient", JavaExec::class) {
        this.javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    }
}

tasks.processResources {
    inputs.property("version", version)
    filesMatching("mcmod.info") {
        expand("version" to version)
    }
}

