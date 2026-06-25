plugins {
    alias(libs.plugins.you.plot.android.feature)
}

android {
    namespace = "com.you.plot.feature.settings"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
}
