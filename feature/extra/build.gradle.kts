plugins {
    alias(libs.plugins.you.plot.android.feature)
}

android {
    namespace = "com.you.plot.feature.extra"
}

dependencies {
    implementation(project(":core:common"))
}
