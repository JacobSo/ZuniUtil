# Developer
Tool library named Zuni for android developer.

#Usage
This Library is ready to be used via jitpack.io. Simply add the following code to your root build.gradle:

        allprojects {
            repositories {
                jcenter()
                maven { url "https://jitpack.io" }
            }
        }
Now add the gradle dependency in your application's build.gradle:

        dependencies {
            compile 'com.github.JacobSo:Developer:62e74c1834'
        }