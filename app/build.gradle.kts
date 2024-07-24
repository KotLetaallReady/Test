plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("androidx.navigation.safeargs.kotlin")
    id ("kotlin-kapt")
}

android {
    namespace = "com.example.test"
    compileSdk = 34

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }



    defaultConfig {
        applicationId = "com.example.test"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
    testImplementation("junit:junit:4.13.2")
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation ("androidx.room:room-runtime:2.6.0")
    kapt ("androidx.room:room-compiler:2.6.0")
    implementation ("androidx.room:room-ktx:2.6.0")


    // Ktor client dependencies
    implementation("io.ktor:ktor-client-core:2.3.1")
    implementation("io.ktor:ktor-client-okhttp:2.3.1")
    implementation("io.ktor:ktor-client-logging:2.3.1")
    implementation("io.ktor:ktor-client-android:2.3.1")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.1")
    implementation("io.ktor:ktor-client-websockets:2.3.1")

    // Ktor server dependencies
    implementation("io.ktor:ktor-server-core:2.3.1")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.1")
    implementation("io.ktor:ktor-server-call-logging:2.0.0")
    implementation("io.ktor:ktor-server-default-headers:2.0.0")
    implementation ("io.ktor:ktor-server-netty:2.3.1")
    implementation("io.ktor:ktor-server-websockets:2.3.1")

    // The core module that provides APIs to a shell
    implementation ("com.github.topjohnwu.libsu:core:6.0.0")

    // Optional: APIs for creating root services. Depends on ":core"
    implementation ("com.github.topjohnwu.libsu:service:6.0.0")

    // Optional: Provides remote file system support
    implementation ("com.github.topjohnwu.libsu:nio:6.0.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}