plugins {
    kotlin("jvm")
    idea
}
dependencies {
    compileOnly(project(":Platform:Core"))
}