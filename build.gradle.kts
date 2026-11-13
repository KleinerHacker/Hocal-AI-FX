/*
 * Copyright (c) KleinerHacker alias Pfeiffer C Soft 2026.
 * This work is licensed under the Apache License, Version 2.0.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations.
 */

import com.github.jk1.license.render.ReportRenderer

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.20" apply false
    id("org.jetbrains.dokka") version "2.3.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.9" apply false
    id("com.github.jk1.dependency-license-report") version "3.1.5" apply false
    id("org.cyclonedx.bom") version "3.5.1" apply false
    id("app.cash.licensee") version "1.14.1" apply false
}

val junitVersion = "6.1.3"

// The UI module shipping the distribution; the licence report and the API docs are taken from it.
val uiProject = ":app:hocal-ui"

allprojects {
    group = "org.pcsoft.app.hocal"

    // A release passes the tag as -PreleaseVersion=<tag>; a local build stays on the snapshot.
    version = (project.findProperty("releaseVersion") as String?)?.takeIf { it.isNotBlank() } ?: "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "com.github.jk1.dependency-license-report")
    apply(plugin = "org.cyclonedx.bom")
    apply(plugin = "app.cash.licensee")

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:${junitVersion}")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
        // Since JUnit 6 the platform launcher is no longer contributed automatically.
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    // Every module writes its licence report into the root build dir, so the MkDocs tasks find one place.
    extensions.configure<com.github.jk1.license.LicenseReportExtension> {
        outputDir = rootProject.layout.buildDirectory.dir("licences").get().asFile.absolutePath
        renderers = arrayOf<ReportRenderer>(
            com.github.jk1.license.render.JsonReportRenderer(),
            com.github.jk1.license.render.SimpleHtmlReportRenderer()
        )
    }

    extensions.configure<app.cash.licensee.LicenseeExtension> {
        listOf(
            "Apache-2.0",
            // ControlsFX
            "BSD-2-Clause",
            // SLF4J
            "MIT",
        ).forEach(::allow)

        // JavaFX publishes GPL-2.0 with Classpath Exception, but only as a URL - it carries no SPDX id
        // in its POM, so it has to be allowed by that URL.
        allowUrl("https://openjdk.java.net/legal/gplv2+ce.html") {
            because("GPL-2.0 with Classpath Exception")
        }

        // typetools names Apache-2.0 only as a plain http URL in its POM, without an SPDX id.
        allowUrl("http://apache.org/licenses/LICENSE-2.0") {
            because("Apache-2.0")
        }

        // MvvmFX's doc-annotations carries the MIT URL wrapped in quotes in its POM, so no SPDX id
        // is derived from it and it has to be allowed by that exact URL.
        allowUrl("'http://opensource.org/licenses/mit-license'") {
            because("MIT")
        }

        // SLF4J names MIT only by the URL of the licence text in its POM, without an SPDX id.
        allowUrl("https://opensource.org/license/mit") {
            because("MIT")
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks {
    //region Dokka
    register<Copy>("copyDokka") {
        group = "dokka"
        description = "Copy all Dokka to MkDocs"
        from(project(uiProject).layout.buildDirectory.dir("dokka"))
        into(File("docs/docs/dokka"))
        dependsOn("$uiProject:dokkaGeneratePublicationHtml")
    }

    register<Delete>("deleteDokka") {
        group = "dokka"
        description = "Delete Dokka"
        delete(File("docs/docs/dokka"))
    }
    //endregion

    //region Licencing
    register<Copy>("copyLicenceReport") {
        group = "licencing"
        description = "Copy licence report to MkDocs"
        from(layout.buildDirectory.dir("licences"))
        into(File("docs/docs/licences"))
        dependsOn("$uiProject:generateLicenseReport")
    }

    register<Delete>("deleteLicenceReport") {
        group = "licencing"
        description = "Delete licence report"
        delete(File("docs/docs/licences"))
    }
    //endregion

    //region MkDocs
    // mike spawns `mkdocs` as a subprocess; on Windows the Python Scripts dir
    // (where mkdocs.exe lives) is often not on PATH. Resolve it once and prepend
    // it to PATH for the mike tasks. In CI (setup-python) it is already on PATH.
    val pythonScriptsDir: String? by lazy {
        runCatching {
            providers.exec {
                commandLine("python", "-c", "import sysconfig; print(sysconfig.get_path('scripts'))")
            }.standardOutput.asText.get().trim().ifEmpty { null }
        }.getOrNull()
    }

    fun Exec.withMikePath() {
        pythonScriptsDir?.let { dir ->
            environment("PATH", dir + File.pathSeparator + System.getenv("PATH"))
        }
    }

    register<Exec>("installMkDocs") {
        group = null
        description = "Install mkdocs"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "mkdocs")
    }

    register<Exec>("installMkDocsMaterial") {
        group = null
        description = "Install mkdocs-material"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "mkdocs-material")
    }

    register<Exec>("installGitHubPages") {
        group = null
        description = "Install ghp-import"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "ghp-import")
    }

    register<Exec>("installMike") {
        group = null
        description = "Install mike for versioned docs deployment"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "mike")
    }

    register("installDocs") {
        group = "MKDocs"
        description = "Install mkdocs and dependencies"
        dependsOn("installMkDocs")
        dependsOn("installMkDocsMaterial")
        dependsOn("installGitHubPages")
        dependsOn("installMike")
    }

    register<Exec>("runDocs") {
        group = "MKDocs"
        description = "Run mkdocs serve and open browser (no version selector - that only appears on the deployed site)"
        workingDir = file("docs")
        commandLine("python", "-m", "mkdocs", "serve", "-o", "-w", ".", "-w", "./docs")
        dependsOn("installDocs", "copyDokka", "copyLicenceReport")
        finalizedBy("deleteDokka", "deleteLicenceReport")
    }

    register<Exec>("buildDocs") {
        group = "MKDocs"
        description =
            "Build the mkdocs site into build/docs (per mkdocs.yml site_dir; no serve, no deploy) - usable as a generation test"
        workingDir = file("docs")
        // --strict fails the build on warnings (broken links, missing pages ...) so it acts as a test;
        // --clean wipes the previous output first.
        commandLine("python", "-m", "mkdocs", "build", "--clean", "--strict")
        dependsOn("installDocs", "copyDokka", "copyLicenceReport")
        finalizedBy("deleteDokka", "deleteLicenceReport")
    }

    register<Exec>("deployDocs") {
        group = "MKDocs"
        description =
            "Deploy a versioned docs snapshot via mike. Pass -PdocsVersion=<tag>; falls back to \"snapshot\" if no tag is given. Requires a pre-configured git push target."
        workingDir = file("docs")
        val ver = (project.findProperty("docsVersion") as String?)?.takeIf { it.isNotBlank() }
            ?: "snapshot"
        val setLatest = ver != "snapshot" && (project.findProperty("setLatest") as String?) != "false"
        val args = buildList {
            add("python"); add("-c"); add("from mike.driver import main; main()"); add("deploy"); add("--push")
            if (setLatest) {
                add("--update-aliases"); add(ver); add("latest")
            } else add(ver)
        }
        commandLine(args)
        withMikePath()
        dependsOn("installDocs", "copyDokka", "copyLicenceReport")
        finalizedBy("deleteDokka", "deleteLicenceReport")
    }

    register<Exec>("setDefaultDocs") {
        group = "MKDocs"
        description =
            "Set the default docs version shown at the root URL via mike (run once after the first release deploy)."
        workingDir = file("docs")
        commandLine("python", "-c", "from mike.driver import main; main()", "set-default", "--push", "latest")
        withMikePath()
        dependsOn("installDocs")
    }
    //endregion
}
