import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.htfyun.eink.zhd.llm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.htfyun.eink.zhd.rkllm"
        minSdk = 29
        targetSdk = 35
        versionCode = 10000
        versionName = "1.00.00"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("debug")

        ndk {
            abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a"))
            //abiFilters.addAll(arrayOf("arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
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

    buildFeatures {
       aidl=true
    }

    // 自动复制 libs 到 jniLibs
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }


    signingConfigs {
        getByName("debug") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
            keyAlias = "platform"
            keyPassword = "android"
            storeFile = file("platform.keystore")
            storePassword = "android"
        }
        register("release") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
            keyAlias = "platform"
            keyPassword = "android"
            storeFile = file("platform.keystore")
            storePassword = "android"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    // 输出类型
    android.applicationVariants.all {
        // 编译类型
        val buildType = this.buildType.name
        //val date = SimpleDateFormat("yyyy-MM-dd-HHmmss").format(Date())
        outputs.all {
            // 判断是否是输出 apk 类型
            if (this is com.android.build.gradle
                .internal.api.ApkVariantOutputImpl
            ) {
                this.outputFileName = "LLMForAndroid_v" + android.defaultConfig.versionName + "_" + buildType + ".apk"
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.appcompat)
}