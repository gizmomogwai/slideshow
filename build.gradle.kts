plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    jcenter()
}

dependencies {
    implementation("com.drewnoakes:metadata-extractor:2.11.0")
    implementation("com.google.guava:guava:27.1-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
}

application {
    mainClassName = "com.flopcode.slideshow.Main"
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = application.mainClassName
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        //archiveBaseName.set("shadow")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to application.mainClassName))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
