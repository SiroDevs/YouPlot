plugins {
    alias(libs.plugins.you.plot.android.feature)
    alias(libs.plugins.you.plot.android.library.compose)
}

android {
    namespace = "com.you.plot.feature.tracker"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(libs.androidx.foundation)
    implementation(libs.play.services.location)
    implementation(libs.osmdroid.android)
}
