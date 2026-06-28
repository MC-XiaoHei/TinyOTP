plugins {
    id("java")
    id("application")
}

group = "cn.xor7.xiaohei.tinyotp"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    modularity.inferModulePath.set(false)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
    implementation("com.github.hstyi:java-winrt-hello:0.0.1")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("com.formdev:flatlaf:3.4.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.7.0")
}

application {
    mainClass.set("cn.xor7.xiaohei.tinyotp.TinyOtpApp")
}

val testTmpDir = file("${layout.buildDirectory.get()}/tmp/test")
tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Djava.io.tmpdir=${testTmpDir.absolutePath}")
    doFirst { testTmpDir.mkdirs() }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
