import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jreleaser.model.Active

plugins {
    id("java-library")
    id("maven-publish")
    alias(libs.plugins.jreleaser.publish)
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines)
}

publishing {
    publications {
        create<MavenPublication>("snapshot") {
            groupId = "io.github.droidlin"
            artifactId = "ioBinder-core"
            version = project.properties["component.version"].toString()

            from(components["java"])
        }
    }
}


jreleaser {
    deploy {
        maven {
            mavenCentral {
                create("snapshot") {
                    url = "https://central.sonatype.com/api/v1/publisher"
                    namespace = "io.github.droidlin"
                    sign = true
                    checksums = true
                    sourceJar = true
                    javadocJar = true

                    stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())

                    artifactOverride {
                        groupId = "io.github.droidlin"
                        artifactId = "ioBinder-compiler"
                        jar = true
                        sourceJar = true
                        javadocJar = true
                    }
                }
            }
        }
    }
}