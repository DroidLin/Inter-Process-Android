// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
}

(rootProject.properties as MutableMap<String, Any>)["buildVer"] = System.currentTimeMillis()
(rootProject.properties as MutableMap<String, Any>)["version"] = (1002002).toLong()