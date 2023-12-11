import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.9.21"

plugins {
    application
    java
    kotlin("jvm") version "1.9.21"
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(
        "com.github.UnitTestBot.klogic:klogic-core:0e37cb3a22"
    ) // core
    implementation(
        "com.github.UnitTestBot.klogic:klogic-utils:0e37cb3a22"
    ) // util terms


    implementation("org.jacodb:jacodb-core:1.3.1")
    implementation("org.jacodb:jacodb-analysis:1.3.1")

    implementation("org.jgrapht:jgrapht-core:1.4.0")
    implementation(files("lib/klogic.jar"))
}

sourceSets {
    val samples by creating {
        java {
            srcDir("src/samples/java")
        }
    }
}

tasks {
    register<Jar>("testJar") {
        group = "jar"
        shouldRunAfter("compileTestKotlin")
        archiveClassifier.set("test")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        val contents = sourceSets.getByName("samples").output

        from(contents)
        dependsOn(getByName("compileSamplesJava"), configurations.testCompileClasspath)
        dependsOn(configurations.compileClasspath)
    }
}


tasks.withType<JavaCompile>() {
    options.compilerArgs.addAll(listOf("-target", "1.8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = freeCompilerArgs + "-Xallow-result-return-type" + "-Xsam-conversions=class" + "-Xcontext-receivers"
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        outputs.upToDateWhen { false }
        showStandardStreams = true
    }

    minHeapSize = "512m"
    maxHeapSize = "2048m"
}

tasks.getByName("compileTestKotlin").finalizedBy("testJar")
