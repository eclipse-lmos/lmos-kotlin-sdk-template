import org.springframework.boot.gradle.tasks.run.BootRun
import java.net.URI

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.citi.helm") version "2.2.0"
    id("com.citi.helm-publish") version "2.2.0"
}

group = "org.eclipse.lmos"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}


tasks.test {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Replace the following with the starter dependencies of specific modules you wish to use
    implementation("org.eclipse.thingweb:kotlin-wot-binding-http:0.1.0-SNAPSHOT")
    implementation("org.eclipse.thingweb:kotlin-wot-binding-websocket:0.1.0-SNAPSHOT")
    implementation("org.eclipse.lmos:lmos-kotlin-sdk-client:0.1.0-SNAPSHOT")
    implementation("org.eclipse.lmos:lmos-kotlin-sdk-server:0.1.0-SNAPSHOT")
    implementation("org.eclipse.lmos:arc-spring-boot-starter:0.1.0-SNAPSHOT")

    implementation("dev.langchain4j:langchain4j-azure-open-ai:1.0.0-beta1")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("app.cash.turbine:turbine:1.2.0")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}

springBoot {
    mainClass.set("org.eclipse.lmos.template.AgentApplicationKt")
}

tasks.register("downloadOtelAgent") {
    doLast {
        val agentUrl =
            "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar"
        val agentFile = file("${project.buildDir}/libs/opentelemetry-javaagent.jar")

        // Ensure directory exists before downloading
        agentFile.parentFile.mkdirs()

        if (!agentFile.exists()) {
            println("Downloading OpenTelemetry Java Agent...")
            agentFile.writeBytes(URI(agentUrl).toURL().readBytes())
            println("Download completed: ${agentFile.absolutePath}")
        } else {
            println("OpenTelemetry Java Agent already exists: ${agentFile.absolutePath}")
        }
    }
}

tasks.named<BootRun>("bootRun") {
    dependsOn("downloadOtelAgent")
    jvmArgs = listOf(
        "-javaagent:${project.buildDir}/libs/opentelemetry-javaagent.jar"
    )
    systemProperty("otel.java.global-autoconfigure.enabled", "true")
    systemProperty("otel.traces.exporter", "otlp")
    systemProperty("otel.exporter.otlp.endpoint", "http://localhost:4318")
    systemProperty("otel.service.name", "chat-agent")
    //systemProperty("otel.javaagent.debug", "true")
}

fun getProperty(propertyName: String) = System.getenv(propertyName) ?: project.findProperty(propertyName) as String


helm {
    charts {
        create("main") {
            chartName.set("${project.name}-chart")
            chartVersion.set("${project.version}")
            sourceDir.set(file("src/main/helm"))
        }
    }
}

tasks.register("replaceChartVersion") {
    doLast {
        val chartFile = file("src/main/helm/Chart.yaml")
        val content = chartFile.readText()
        val updatedContent = content.replace("\${chartVersion}", "${project.version}")
        chartFile.writeText(updatedContent)
    }
}

tasks.register("helmPush") {
    description = "Push Helm chart to OCI registry"
    group = "helm"
    dependsOn(tasks.named("helmPackageMainChart"))

    doLast {
        val registryUrl = getProperty("REGISTRY_URL")
        val registryUsername = getProperty("REGISTRY_USERNAME")
        val registryPassword = getProperty("REGISTRY_PASSWORD")
        val registryNamespace = getProperty("REGISTRY_NAMESPACE")

        helm.execHelm("registry", "login") {
            option("-u", registryUsername)
            option("-p", registryPassword)
            args(registryUrl)
        }

        helm.execHelm("push") {
            args(tasks.named("helmPackageMainChart").get().outputs.files.singleFile.toString())
            args("oci://$registryUrl/$registryNamespace")
        }

        helm.execHelm("registry", "logout") {
            args(registryUrl)
        }
    }
}


repositories {
    //mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}
