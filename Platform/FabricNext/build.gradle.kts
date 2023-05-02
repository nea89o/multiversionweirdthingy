plugins {
    id("xyz.wagyourtail.unimined")
    kotlin("jvm")
    idea
}

minecraft {
    fabric()
}


dependencies {
    "fabric"("net.fabricmc:fabric-loader:0.14.19")
    mappings("net.fabricmc:intermediary:1.19.4:v2")
    mappings("net.fabricmc:yarn:1.19.4+build.2:v2")
    minecraft("net.minecraft:minecraft:1.19.4")
    implementation(project(":Platform:Core"))
    modImplementation("net.fabricmc:fabric-language-kotlin:1.9.3+kotlin.1.8.20")
    modImplementation("net.fabricmc.fabric-api:fabric-command-api-v2:2.2.5+e719b857f4")
    modImplementation("net.fabricmc.fabric-api:fabric-lifecycle-events-v1:2.2.15+5da15ca1f4")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.1.2")
}

tasks.processResources {
    inputs.property("version", version)
    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}
