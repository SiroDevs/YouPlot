plugins {
    alias(libs.plugins.you.plot.android.library)
    alias(libs.plugins.you.plot.hilt)
}

android {
    namespace = "com.you.plot.core.data"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:database"))

    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
}
