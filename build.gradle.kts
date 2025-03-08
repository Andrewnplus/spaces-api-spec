plugins {
    kotlin("jvm") version "2.1.10"
    id("org.openapi.generator") version "7.12.0"
    id("com.diffplug.spotless") version "7.0.2"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

val ktorVersion = "3.1.1"
val serializationVersion = "1.8.0"

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/spaces-api.yaml")
    outputDir.set("$projectDir/build/generated")
    configOptions.set(
        mapOf(
            "library" to "jvm-ktor",
            "serializationLibrary" to "kotlinx_serialization",
            "useCoroutines" to "true",
            "dateLibrary" to "java8",
            "generateModelDocumentation" to "true",
            "generateApiDocumentation" to "true",
        ),
    )
}

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.5.0")
    }

    yaml {
        target("**/*.yaml", "**/*.yml")
        jackson()
    }

    json {
        target("**/*.json")
        gson()
    }
}

tasks.named("build") {
    dependsOn("spotlessApply")
    dependsOn("openApiGenerate")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/YOUR_GITHUB_USERNAME/YOUR_REPO")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("githubPackages") {
            from(components["java"])
            groupId = "io.nplus.space"
            artifactId = "spaces-api-spec"
            val versionFromTag: String? = System.getenv("GITHUB_REF")?.replace("refs/tags/", "")
            version = versionFromTag ?: "0.1.0-SNAPSHOT"
        }
    }
}
