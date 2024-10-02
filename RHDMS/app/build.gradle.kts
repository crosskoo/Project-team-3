 plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.jeyun.rhdms"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jeyun.rhdms"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    viewBinding {
        enable = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.work.runtime)
    implementation(libs.fragment)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.blessed.android)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.sql2o)
    implementation(libs.mpandroidchart)
    implementation(libs.jtds)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.volley)
}