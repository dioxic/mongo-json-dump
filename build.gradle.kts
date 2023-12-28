plugins {
    id("com.github.ben-manes.versions") version "0.50.0"
    application
}

group = "com.mongodb"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.11.1")
    implementation(platform("io.projectreactor:reactor-bom:2023.0.1"))
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.apache.logging.log4j:log4j-core:2.22.1")
    implementation("org.apache.logging.log4j:log4j-api:2.22.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1")
    implementation("io.projectreactor:reactor-core")
    implementation("info.picocli:picocli:4.7.5")
    annotationProcessor("info.picocli:picocli-codegen:4.7.5")
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<JavaCompile> {
    val compilerArgs = options.compilerArgs
    compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

application {
    mainClass.set("com.mongodb.jsondump.Application")
}

distributions {
    main {
        distributionBaseName.set("mongojsondump")
    }
}

tasks.test {
    useJUnitPlatform()
}