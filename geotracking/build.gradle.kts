plugins {
    id("com.android.library")
id("maven-publish")
//    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    id("realm-android")

}

//group = "com.gitlab.KhurseedAnsari"

android {
    namespace = "com.innov.geotracking"
    compileSdk = 34

    defaultConfig {
//        applicationId = "com.innov.geotracking"
        minSdk = 24
        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"

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

    buildFeatures {
        buildConfig = true
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }



}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.glide)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.kotlin.bom)
    implementation(libs.koin.test)
    implementation(libs.koin.test.junit4)


    implementation(libs.multidex)
    implementation(libs.branch)

    implementation(libs.picasso)
    implementation(libs.glide)
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
    implementation(libs.play.services.location)
    implementation(libs.realm)
    implementation(libs.play.services.maps)
    implementation(libs.krealmextensions)
    implementation(libs.androidx.recyclerview)


}


publishing {
    repositories {
        maven {
            name = "com.innov.geotracking"
            url = uri("https://gitlab.com/pranaypatil7744/all_resources.git")
            credentials {
                username = "Khurseed Ansari"
                password = "glpat-yVhPk2uKyM1sj5xxaHov"
            }
        }
    }


/*    publications {
        aar
        aar(MavenPublication) {
            groupId 'com'
            artifactId 'test'
            version '1.0.0'
            artifact("set your aar file location")
        }
    }*/
}

/*publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                name = "Innov Geotracking"
                description = "track the location and store lat ,long in realm db"
                url = "https://gitlab.com/"
                properties = mapOf(
                    "myProp" to "value",
                    "prop.with.dots" to "anotherValue"
                )
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "12085525"
                        name = "Khurseed Ansari"
                        email = "khurseeda@innov.in"
                    }
                }
                scm {
                    connection = "https://gitlab.com/pranaypatil7744/all_resources.git"
                    developerConnection = "git@gitlab.com:pranaypatil7744/all_resources.git"
                    url = "https://gitlab.com/pranaypatil7744/all_resources/-/tree/all_geotracking/app/src/main"
                }
            }
        }
    }
}*/

