package dev.olog.msc

import org.gradle.api.JavaVersion

@Suppress("ClassName")
object config {

    const val minSdk = 21
    const val targetSdk = 33
    const val compileSdk = targetSdk

    /*  version code
        999 - for old compatibility
        27 - android version
        X.xxx - X major version, xxx minor version
     */
    const val versionCode = 999_33_4_0_00
    const val versionName = "4.0.0"

    val javaVersion = JavaVersion.VERSION_11

}