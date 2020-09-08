plugins {
    kotlin("jvm")
    id("application")
}

repositories {
    jcenter()
}

application {
    mainClass.set("net.rubygrapefruit.gen.MainKt")
}