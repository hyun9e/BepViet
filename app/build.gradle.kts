plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.bepviet02"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bepviet02"
        minSdk = 24
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //noinspection UseTomlInstead
    // Import the BoM for the Firebase platform
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    // Add the dependency for the Realtime Database library
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    // Glide for loading image
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // ImagePicker Library
    implementation("com.github.Dhaval2404:ImagePicker:2.1")
    // Cloudinary Java SDK
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    // Progress Dialog
    implementation("io.github.tashilapathum:please-wait:0.5.0")
    // Circle image view
    implementation("de.hdodenhof:circleimageview:3.1.0")
//    // JUnitt & Robolectric
//    testImplementation("junit:junit:4.13.2")
//    testImplementation("org.robolectric:robolectric:4.13")


    // JUnit for unit testing
    testImplementation("junit:junit:4.13.2")

    // Mockito for mocking dependencies
    testImplementation("org.mockito:mockito-core:5.4.0")
    androidTestImplementation("org.mockito:mockito-android:5.4.0")

    // Android Testing Support
    testImplementation ("androidx.test:runner:1.5.0")
    androidTestImplementation("androidx.test:rules:1.6.1")
    testImplementation ("androidx.test:core:1.5.0")
    testImplementation ("androidx.test.ext:junit:1.1.5")
    testImplementation ("androidx.arch.core:core-testing:2.2.0")

    // Robolectric (Optional: for running tests without emulator/device)
    testImplementation("org.robolectric:robolectric:4.10.3")
    // Espresso (Optional: for UI testing)
    androidTestImplementation("`androidx.test.espresso:espresso-c`ore:3.6.1")
}