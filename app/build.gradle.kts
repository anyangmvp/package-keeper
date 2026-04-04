import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties

// ========== 可配置常量 ==========
object AppConfig {
    const val APP_NAME = "快递管家"                    // 应用名称
    const val DATE_FORMAT = "yyyyMMdd"                       // 日期格式
    val buildDate: String
        get() = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT))
}

// 从 local.properties 读取签名配置
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

android {
    namespace = "anyang.mypackages"
    compileSdk = 36

    defaultConfig {
        applicationId = "anyang.mypackages"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        // 版本号使用日期格式
        versionName = AppConfig.buildDate

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val keyStoreFile = System.getenv("KEYSTORE_FILE") ?: localProperties.getProperty("KEYSTORE_FILE")
            if (!keyStoreFile.isNullOrBlank()) {
                signingConfig = signingConfigs.create("release") {
                    storeFile = file(keyStoreFile)
                    storePassword = System.getenv("KEYSTORE_PASSWORD") ?: localProperties.getProperty("KEYSTORE_PASSWORD", "")
                    keyAlias = System.getenv("KEY_ALIAS") ?: localProperties.getProperty("KEY_ALIAS", "")
                    keyPassword = System.getenv("KEY_PASSWORD") ?: localProperties.getProperty("KEY_PASSWORD", "")
                }
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
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

    // APK 输出文件名: PackageKeeper-20260208.apk
    androidComponents {
        onVariants { variant ->
            variant.outputs.forEach { output ->
                if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
                    output.outputFileName = "${AppConfig.APP_NAME}-${AppConfig.buildDate}.apk"
                }
            }
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// ========== 自动拷贝 APK 到桌面 ==========
tasks.register<Copy>("copyApkToDesktop") {
    val desktopDir = File(System.getProperty("user.home"), "Desktop/AppOutputs")

    from(layout.buildDirectory.dir("outputs/apk/release"))
    into(desktopDir)
    include("*.apk")

    doFirst {
        desktopDir.mkdirs()
        println("📦 正在拷贝 APK 到: ${desktopDir.absolutePath}")
    }
    doLast {
        println("✅ APK 已拷贝到桌面 AppOutputs 文件夹")
    }
}

afterEvaluate {
    // 绑定到 assembleRelease 任务
    tasks.named("assembleRelease").configure { finalizedBy("copyApkToDesktop") }
    // 绑定到 installRelease 任务
    tasks.findByName("installRelease")?.let { it.finalizedBy("copyApkToDesktop") }
}