plugins {
    alias(libs.plugins.swahilib.android.library.compose)
}

android {
    namespace = "com.you.plot.core.ui"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:domain"))
    api(project(":core:designsystem"))

    implementation(libs.androidx.foundation)
    implementation(libs.androidx.compose.livedata)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.osmdroid.android)
}
