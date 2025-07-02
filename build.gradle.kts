plugins {
    id("java-library")
    id("maven-publish")
}

group = "com.github.nohsw"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    useJUnitPlatform()
    
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
    options.encoding = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("CIDR Validator")
                description.set("A comprehensive Java library for CIDR notation validation, calculation, and normalization")
                url.set("https://github.com/sungwoong-Noh/cidr-validator")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("nohsw")
                        name.set("Seongwoog-Noh")
                        email.set("your-email@example.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/sungwoong-Noh/cidr-validator.git")
                    developerConnection.set("scm:git:ssh://github.com/sungwoong-Noh/cidr-validator.git")
                    url.set("https://github.com/sungwoong-Noh/cidr-validator")
                }
            }
        }
    }
}