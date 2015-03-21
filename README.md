LibgdxUtils
===========

This is my hacky, undocumented library to go with LibGDX. This is mostly a collection of code I used to write at the start of every gamejam, that I have now compiled into this package to make my life a bit easier.

**Building Code**

    mvn clean install

**Importing libgdx-utils into your libgdx project**

This library is available as a Maven repository, which makes integrating this with your libgdx application painless if you use gradle. First add the repository libgdx-utils is located in to your main build.gradle file, which should look like this:

```
...
allprojects {
    apply plugin: "eclipse"
    apply plugin: "idea"
    
    version = "1.0"
    ext {
        appName = "UtilsTest"
        gdxVersion = "1.5.0"
        roboVMVersion = "0.0.11"
    }
    
    repositories {
        mavenLocal();
        mavenCentral()
        maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
        maven { url "http://walnoot.me:8080/plugin/repository/everything/" }
    }
}
...
```

Then, add libgdx-utils to the core project like this:
```
...
project(":core") {
    apply plugin: "java"
    
    dependencies {
        compile "com.badlogicgames.gdx:gdx:$gdxVersion"
        compile "me.walnoot:libgdx-utils:1.0-SNAPSHOT"
    }
}
...
```
