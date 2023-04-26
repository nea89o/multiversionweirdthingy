pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.wagyourtail.xyz/releases")
    }
}
include("Platform:Core")
include("Platform:Forge189")
include("Platform:FabricNext")
include("Modules:Core")
