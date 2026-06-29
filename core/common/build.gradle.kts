plugins {
    alias(libs.plugins.you.plot.android.library)
}

android {
    namespace = "com.you.plot.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.ktor.client.android)
    implementation(libs.androidx.foundation)
    implementation(libs.osmdroid.android)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
}
