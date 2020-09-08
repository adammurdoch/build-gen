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

dependencies {
    implementation("net.rubygrapefruit:native-platform:0.22-milestone-7")
}