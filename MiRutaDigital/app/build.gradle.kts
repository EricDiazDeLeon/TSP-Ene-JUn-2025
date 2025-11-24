import java.util.Properties



plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.mirutadigital"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mirutadigital"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // lee la clave del archivo local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }
        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") // se debe llamar igual y sin "" MAPS_API_KEY = 4498...

        // para que este la clave disponible como un recurso de compilacion
        resValue("string", "maps_api_key", mapsApiKey)
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

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    // -- AnroidX core y ciclo de vida --
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // -- compose --
    // bom - gestiona las versiones de las librerias de compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    // navegacion de compose
    implementation(libs.androidx.navigation.compose)

    // -- material 3 --
    implementation(libs.androidx.material3)
    // extencion de iconos de material
    implementation(libs.androidx.compose.material.icons.extended)

    // -- google maps y localizacion --
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.maps.utils.ktx)

    // -- Retrofit y Moshi para Networking --
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.squareup.moshi.kotlin)
    implementation(libs.logging.interceptor)

    // -- room - base de datos --
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // soporte para corrutinas de Kotlin
    implementation(libs.androidx.room.paging)

    ksp(libs.androidx.room.compiler) // para usar ksp con room - procesador de anotaciones

    // -- firebase --
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    //implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // -- Gson --
    implementation(libs.gson)

    // -- testing --
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // -- debug --
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}