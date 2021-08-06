plugins {
    kotlin("jvm")
    id("application")
}

repositories {
    jcenter()
}

application {
    applicationName = "build-gen"
    mainClass.set("net.rubygrapefruit.gen.MainKt")
}

dependencies {
    implementation("net.rubygrapefruit:native-platform:0.22-milestone-7")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation(gradleTestKit())
}