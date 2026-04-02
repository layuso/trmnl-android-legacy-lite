plugins { id("com.android.application") }

android {
  namespace = "com.trmnl.legacylite"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.trmnl.legacylite"
    minSdk = 22
    targetSdk = 34
    versionCode = 1
    versionName = "0.0.1"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

dependencies {
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("com.google.android.material:material:1.11.0")
}
