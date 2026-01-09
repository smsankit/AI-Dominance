plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    id("jacoco")
}

android {
    namespace = "com.example.logger"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.logger"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)
    // Lifecycle Compose + ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Network: Retrofit + Gson + OkHttp logging
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Coil for image loading
    implementation(libs.coil.compose)

    // Hilt Navigation for Compose
    implementation(libs.androidx.hilt.navigation.compose)

    // javax.inject annotations
    implementation(libs.javax.inject)

    // Material Icons Extended
    implementation(libs.androidx.material.icons.extended)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Coroutine testing (match coroutines version 1.9.0)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    // Turbine for testing
    testImplementation("app.cash.turbine:turbine:1.0.0")
    // Mockito for mocking (use correct version for Kotlin 2.x)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.mockito:mockito-core:5.2.0")
    // JUnit for testing
    testImplementation("junit:junit:4.13.2")
    // Robolectric for Android main looper mocking in unit tests
    testImplementation("org.robolectric:robolectric:4.11.1")
}

kapt {
    correctErrorTypes = true
}

jacoco {
    toolVersion = "0.8.11"
}

// JaCoCo test coverage report task
// This will generate HTML and XML reports after running tests
// You can view the HTML report in app/build/reports/jacoco/test/html/index.html

// Only configure if not already present
// language=kotlin

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val includes = listOf(
        "**/presentation/**/*ViewModel.class",
        "**/presentation/**/*UiState.class",
        "**/presentation/**/*ScreenKt.class",
        "**/domain/usecase/*UseCase.class"
    )
    val fileFilter = listOf(
        // Exclude generated, test, di, and Android classes
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "**/Hilt*.*",
        "**/Dagger*.*",
        "**/*_Factory*.*",
        "**/*_Hilt*.*",
        "**/*_MembersInjector*.*",
        "**/databinding/**",
        "**/android/databinding/**",
        "**/androidx/databinding/**",
        "**/BR.*",
        "**/ComposableSingletons*.*",
        "**/*Composable*.*",

        // Coroutines
        "**/*FlowCollector*.*",
        "**/*SuspendLambda*.*",

        // Anonymous inner classes: $1, $2, etc.
        "**/*\\$[0-9]*.*"
    )
    val javaClasses = fileTree(
        mapOf(
            "dir" to "$buildDir/intermediates/javac/debug/classes",
            "includes" to includes,
            "excludes" to fileFilter
        )
    )
    val kotlinClasses = fileTree(
        mapOf(
            "dir" to "$buildDir/tmp/kotlin-classes/debug",
            "includes" to includes,
            "excludes" to fileFilter
        )
    )
    classDirectories.setFrom(files(javaClasses, kotlinClasses))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(files("$buildDir/jacoco/testDebugUnitTest.exec"))
}
