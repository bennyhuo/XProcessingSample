plugins {
    kotlin("jvm")
    java
    kotlin("kapt")
    id("com.google.devtools.ksp") version "1.6.0-1.0.1"
}

repositories {
    mavenCentral()
}

val useKsp = extra.get("useKsp") == "true"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(project(":annotation"))

    val processor = if (useKsp) "ksp" else "kapt"
    processor(project(":compiler"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

sourceSets.main {
    java.srcDir("build/generated/ksp/main/kotlin")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}