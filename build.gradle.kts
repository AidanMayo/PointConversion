plugins {
    kotlin("jvm") version "2.0.21"
}

group = "org.xephyrous"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}