import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("com.asarkar.gradle.build-time-tracker") version "3.0.1"
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
}

version = "0.0.1"
group = "com.example.kick.graphql.federation"

repositories {
    mavenCentral()
}

dependencies {
    val kickstartVersion = "12.0.0"
    val spring = "5.3.12"
    val springBoot = "2.5.5"

    implementation("com.apollographql.federation:federation-graphql-java-support:0.7.0")
    //better use autoconfigure so end services can use exclude - it is harder to exclude with starters
    implementation("com.graphql-java-kickstart:graphql-spring-boot-autoconfigure:$kickstartVersion")
    implementation("org.springframework:spring-context:$spring")

    testImplementation(platform("org.junit:junit-bom:5.8.0"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.platform:junit-platform-engine")
    testImplementation("org.assertj:assertj-core:3.20.2")

    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("org.awaitility:awaitility-kotlin:4.1.0")
    testImplementation("com.graphql-java-kickstart:graphql-spring-boot-test-autoconfigure:$kickstartVersion")
    testImplementation("org.springframework.cloud:spring-cloud-starter-sleuth:3.0.4")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure:$springBoot")
    testImplementation("org.springframework:spring-test:$spring")
    testImplementation("org.springframework.boot:spring-boot-starter-validation:$springBoot")
    testImplementation("org.springframework.boot:spring-boot-starter-web:$springBoot")
    testImplementation("javax.validation:validation-api:2.0.1.Final")
    testImplementation("org.assertj:assertj-core:3.21.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=enable")
    }

    compileJava {
        targetCompatibility = "11"
    }

    test {
        useJUnitPlatform()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withJavadocJar()
    withSourcesJar()
}

