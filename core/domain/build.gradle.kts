plugins {
    alias(libs.plugins.swahilib.android.library)
    alias(libs.plugins.swahilib.hilt)
}

android {
    namespace = "com.you.plot.core.domain"
}

dependencies {
    api(project(":core:common"))
    implementation(libs.androidx.core.ktx)
}
