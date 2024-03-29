plugins {
    java
}

group = "net.azisaba"
version = "1.1.3"

repositories {
    mavenCentral()

    // velocity repo
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    // velocity-api
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.0.1")

    // luckperms api
    compileOnly("net.luckperms:api:5.4")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}
