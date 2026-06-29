plugins {
    alias(libs.plugins.you.plot.android.library)
    alias(libs.plugins.you.plot.hilt)
}

android {
    namespace = "com.you.plot.core.domain"
}

dependencies {
    api(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
