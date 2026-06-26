plugins {
    alias(libs.plugins.you.plot.android.feature)
    alias(libs.plugins.you.plot.android.library.compose)
}

android {
    namespace = "com.you.plot.feature.route"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))

    implementation(libs.androidx.foundation)
    implementation(libs.osmdroid.android)
    implementation(libs.play.services.location)
}
