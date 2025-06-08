plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.royals.mathshooter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.royals.mathshooter"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add vector drawable support for older devices
        vectorDrawables.useSupportLibrary = true
    }

    // Signing configurations for release builds
    signingConfigs {
        create("release") {
            // Store file path - place your keystore in app/ directory
            storeFile = file("mathshooter.jks")

            // Keystore credentials
            storePassword = "Ss8750257510@"
            keyAlias = "key0"
            keyPassword = "Ss8750257510@"

            // Enable v1 and v2 signature schemes
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }

        // Optional: Debug signing config (uses debug keystore)
       /* getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "Ss8750257510@"
            keyAlias = "Ss8750257510@"
            keyPassword = "Ss8750257510@"
        }*/
    }

    buildTypes {
        release {
            // Enable code shrinking, obfuscation, and optimization
            isMinifyEnabled = true
            isShrinkResources = true

            // Use the release signing config
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Optional: Customize APK name
            setProperty("archivesBaseName", "MathShooter-v${defaultConfig.versionName}")

            // Optimization flags
            isDebuggable = false
            isJniDebuggable = false
            renderscriptOptimLevel = 3

            // Zipalign and optimize
            isZipAlignEnabled = true
            isCrunchPngs = true
        }

        debug {
            // Debug build configuration
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }

        // Optional: Create a staging build type
        create("staging") {
            initWith(getByName("release"))
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-STAGING"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"

        // Kotlin compiler optimizations
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // Bundle configuration for Google Play
    bundle {
        language {
            enableSplit = false
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }

    // Packaging options
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
        }
    }

    // Lint options
    lint {
        abortOnError = false
        checkReleaseBuilds = true
        disable += "MissingTranslation"
    }

    // Test options
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Preferences and data storage
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.datastore.preferences)

    // Animations
    implementation(libs.androidx.dynamicanimation)

    // Additional dependencies for release optimization
    implementation("androidx.multidex:multidex:2.0.1")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Additional testing dependencies
    testImplementation("org.mockito:mockito-core:5.1.1")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
}