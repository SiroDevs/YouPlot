plugins {
    alias(libs.plugins.you.plot.android.library)
    alias(libs.plugins.you.plot.hilt)
    alias(libs.plugins.devtools.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "com.you.plot.core.database"
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}
