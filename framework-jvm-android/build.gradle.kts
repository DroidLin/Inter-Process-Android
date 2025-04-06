import org.jreleaser.model.Active

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jreleaser.publish)
    id("kotlin-kapt")
    id("maven-publish")
}

android {
    namespace = "com.android.inter.process.framework"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "buildVersion", "\"${System.currentTimeMillis()}\"")
            buildConfigField("long", "version", 1001002.toString())
        }

        debug {
            buildConfigField("String", "buildVersion", "\"${System.currentTimeMillis()}\"")
            buildConfigField("long", "version", 1001002.toString())
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        aidl = true
        buildConfig = true
    }
}

dependencies {
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.kotlinx.coroutines)

    implementation(project(":framework"))
    kapt(project(":compiler"))
}

publishing {
    publications {
        create<MavenPublication>("snapshot") {
            groupId = "io.github.droidlin"
            artifactId = "ioBinder-android"
            version = project.properties["component.version"].toString()
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