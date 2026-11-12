plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}
android {
    namespace = "com.swenhao10.localfirst"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.swenhao10.localfirst"
        minSdk = 26
        targetSdk = 35
        versionCode = 12
        versionName = "0.9.0"
    }
    // 固定签名：keystore/zhixing-debug.jks 就是标准的 Android debug 证书（口令 android），
    // 放进仓库是为了让“任何机器重新打包出来的 APK 签名都一样”，可以直接覆盖安装，不用卸载重装。
    // 它不是正式发布用的签名，别拿它上架。
    signingConfigs {
        create("stableDebug") {
            storeFile = rootProject.file("keystore/zhixing-debug.jks")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("stableDebug")
        }
    }
    buildFeatures { compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
}
kotlin { jvmToolchain(21) }
dependencies {
    val bom = platform("androidx.compose:compose-bom:2025.10.01")
    implementation(bom); androidTestImplementation(bom)
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    // 液态玻璃 / 背景模糊：github.com/Kyant0/AndroidLiquidGlass（Apache-2.0）。模糊要 Android 12+，透镜折射要 13+，更老的系统自动退回半透明
    implementation("io.github.kyant0:backdrop:1.0.0")
    implementation("io.github.kyant0:capsule:2.1.1")
}
