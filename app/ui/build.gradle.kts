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

plugins {
    application
    id("org.javamodularity.moduleplugin") version "2.0.1"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "4.2.0"
}

application {
    mainModule.set("org.pcsoft.app.hocal.ui")
    mainClass.set("org.pcsoft.app.hocal.app.LauncherKt")
    applicationName = "hocal-ui"

    // JavaFX loads its native graphics library through System::load. Since JDK 24 that is a restricted
    // call and the JVM warns about it unless the calling module is granted native access up front.
    applicationDefaultJvmArgs = listOf("--enable-native-access=javafx.graphics")
}

javafx {
    version = "25.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

val testFxVersion = "4.0.18"
val log4jVersion = "2.26.1"

dependencies {
    implementation("org.apache.commons:commons-lang3:3.21.0")

    implementation("org.controlsfx:controlsfx:11.2.5")
    implementation("de.saxsys:mvvmfx:1.8.0")

    // The application logs against the SLF4J API only; Log4j 2 is the implementation behind it and
    // is bound through the SLF4J provider, so no code ever touches a Log4j type.
    implementation("org.slf4j:slf4j-api:2.0.19")
    runtimeOnly("org.apache.logging.log4j:log4j-core:${log4jVersion}")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:${log4jVersion}")

    testImplementation("org.testfx:testfx-core:${testFxVersion}")
    // TestFX still pulls the JUnit artifacts of its own generation, which collide with the platform used here.
    testImplementation("org.testfx:testfx-junit5:${testFxVersion}") {
        exclude(group = "org.junit.jupiter")
        exclude(group = "org.junit.platform")
    }
    // Monocle provides the headless glass platform TestFX runs on, so no window is opened.
    testImplementation("org.testfx:openjfx-monocle:21.0.2")
}

tasks.withType<Test> {
    // The UI tests must not open a window.
    systemProperty("testfx.robot", "glass")
    systemProperty("testfx.headless", "true")
    systemProperty("glass.platform", "Monocle")
    systemProperty("monocle.platform", "Headless")
    systemProperty("prism.order", "sw")
    systemProperty("java.awt.headless", "true")
}

// The module plugin only ever puts its own "test" task on the module path in the first place, so
// only that task carries the TestModuleOptions extension; a Test task registered by hand, like
// integrationTest below, already runs on the classpath and needs no override here.
tasks.named<Test>("test") {
    // Monocle reaches into internals of javafx.graphics, which the module path does not allow, and
    // ResourceBundle.Control is unsupported inside a named module - so the tests run on the classpath.
    extensions.getByType(org.javamodularity.moduleplugin.extensions.TestModuleOptions::class.java)
        .runOnClasspath = true

    // Integration tests ("...IT") cover a complete feature or aim at performance, per the `testing`
    // skill; they run apart from the plain developer tests so they can be re-run on their own, but
    // `check` - and with it `build` - always exercises them as well.
    filter { excludeTestsMatching("*IT") }
}

val integrationTest = tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs the integration tests (\"...IT\") of app/ui"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    filter { includeTestsMatching("*IT") }
}

tasks.named("check") {
    dependsOn(integrationTest)
}

jlink {
    // mvvmfx and typetools carry no module descriptor, so they are welded into one merged module
    // whose descriptor is generated and then compiled on its own. That compilation only sees the two
    // staging directories of the plugin, and neither JavaFX nor the Kotlin standard library is staged
    // there - the merged descriptor requires both and javac reports them as not found. Naming them
    // here copies their JARs into the staging directory, so the descriptor resolves.
    addExtraDependencies("javafx", "kotlin")

    imageZip.set(layout.buildDirectory.file("/distributions/hocal-ui-image-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "zip-6", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "hocal-ui"
    }
}

// The shipped archive: the two start scripts at the root, every JAR below "libs". The layout the
// application plugin produces (bin/ + lib/, wrapped in a version folder) is not what we ship, so its
// archive tasks are disabled in favour of `packageDist`.
tasks.named("distZip") { enabled = false }
tasks.named("distTar") { enabled = false }

val packageDist = tasks.register<Zip>("packageDist") {
    group = "distribution"
    description = "Packs the application into a ZIP: start scripts at the root, all JARs in libs"

    archiveBaseName.set("hocal-ui")
    archiveVersion.set(project.version.toString())
    // JavaFX resolves to the classifier of the building host, so the archive names that host.
    archiveClassifier.set(javafx.platform.classifier)
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    // No version folder inside the archive - the scripts sit directly at the root.
    into("")

    from(layout.projectDirectory.dir("src/dist")) {
        filePermissions { unix("0755") }
    }

    into("libs") {
        from(tasks.named("jar"))
        from(configurations.named("runtimeClasspath"))
    }
}

tasks.named("assemble") {
    dependsOn(packageDist)
}
