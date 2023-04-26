plugins {
    id("xyz.wagyourtail.unimined") version "0.4.9" apply false
    kotlin("jvm") version "1.8.10" apply false
}

allprojects {
    apply(plugin = "java")
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io") {
            content { includeGroupByRegex("(com|io)\\.github\\..+") }
        }
        maven("https://repo.nea.moe/releases")
        maven("https://maven.terraformersmc.com/releases")
        maven("https://maven.fabricmc.net")
    }
    extensions.findByType<JavaPluginExtension>()!!.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    project.afterEvaluate {
        tasks.withType(AbstractArchiveTask::class) {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }
    }
    tasks.withType(JavaCompile::class) {
        options.encoding = "UTF-8"
        options.isFork = true
    }

}
