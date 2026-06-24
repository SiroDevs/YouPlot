plugins {
    alias(libs.plugins.swahilib.android.library)
    alias(libs.plugins.swahilib.hilt)
}

android {
    namespace = "com.you.plot.core.data"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:database"))
    api(project(":core:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.play.services.location)
}
