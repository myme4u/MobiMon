plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.mobimon.feature.overlay"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(libs.androidx.core.ktx)
}
