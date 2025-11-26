// This file gives dependencies to the *build* itself (plugins’ classpaths).
// We put JavaPoet here so the Hilt plugin sees a modern version during its tasks.

buildscript {
    repositories { google(); mavenCentral() }
    dependencies {
        classpath("com.squareup:javapoet:1.13.0")
    }
}
