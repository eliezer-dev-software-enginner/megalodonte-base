plugins {
    id("java")
    id("maven-publish")

    // 🛑 CORREÇÃO: Usando o ID e a versão CORRETOS conforme a documentação oficial.
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "megalodonte"
version = "1.0.0-beta"

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}


// 🛑 2. CONFIGURA O PLUGIN DO JAVAFX
javafx {
    // Define a versão do JavaFX para ser usada em todos os módulos
    version = "25.0.1"

    // Lista os módulos JavaFX que sua biblioteca PRECISA para compilar.
    // O plugin adiciona automaticamente a dependência para a sua plataforma de build.
    modules("javafx.controls", "javafx.graphics")
}

dependencies {
    // Megalodonte ecosystem
    // (megalodonte-reactivity é adicionado em runtime via reflexão)
    
    // Dependências de teste (mantidas)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Mockito
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")
    
    // TestFX for JavaFX testing
    testImplementation("org.testfx:testfx-core:4.0.16-alpha")
    testImplementation("org.testfx:testfx-junit5:4.0.16-alpha")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")

    //logs
    implementation("org.slf4j:slf4j-api:2.0.17")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.17")
}

tasks.test {
    //useJUnitPlatform()
    enabled = false
}

tasks.javadoc {
    (options as? org.gradle.external.javadoc.StandardJavadocDocletOptions)?.apply {
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.jar {
    archiveBaseName.set("megalodonte-base")

    manifest {
        attributes(
            "Implementation-Title" to "Megalodonte Base Library",
            "Implementation-Version" to project.version
        )
    }
}

tasks.register<Jar>("javadocJar") {
    archiveBaseName.set("megalodonte-base")
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

// Configuração de Publicação (mantida)
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.named("javadocJar"))
            artifactId = "megalodonte-base"
        }
    }
}

