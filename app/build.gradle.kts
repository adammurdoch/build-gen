plugins {
    kotlin("jvm")
    id("application")
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
    maven { url = uri("https://repo.gradle.org/gradle/public-cache") }
}

application {
    applicationName = "build-gen"
    mainClass.set("net.rubygrapefruit.gen.MainKt")
}

dependencies {
    implementation("net.rubygrapefruit:native-platform:0.22-milestone-21")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation(gradleTestKit())
}