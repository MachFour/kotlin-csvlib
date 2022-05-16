import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "com.machfour"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib:1.6.20"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

tasks.withType<Jar> {
    archiveBaseName.set("kotlin-csvlib")
    manifest.attributes["Manifest-Version"] = 1.0
    duplicatesStrategy = DuplicatesStrategy.WARN
}