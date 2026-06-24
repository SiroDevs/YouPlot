plugins {
    alias(libs.plugins.swahilib.android.library)
}

android {
    namespace = "com.you.plot.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.ktor.client.android)
}
