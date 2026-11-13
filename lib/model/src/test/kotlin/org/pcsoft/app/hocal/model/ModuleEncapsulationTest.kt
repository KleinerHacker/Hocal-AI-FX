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
package org.pcsoft.app.hocal.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.module.ModuleDescriptor
import java.util.Collections

/**
 * Developer tests for the module descriptor of `hocal-model`: what a user interface may use is exported
 * to everybody, what only the project manager of `hocal-fx-model` may use is exported to that module only.
 *
 * The tests run on the class path, where the module system does not restrict anything; they read the
 * descriptor itself, which is what restricts the access on the module path of the application.
 */
class ModuleEncapsulationTest {

    private val managerModule = "org.pcsoft.app.hocal.fx.model"

    private val descriptor: ModuleDescriptor by lazy {
        val own = Collections.list(javaClass.classLoader.getResources("module-info.class")).firstNotNullOfOrNull { url ->
            try {
                url.openStream().use { ModuleDescriptor.read(it) }.takeIf { it.name() == "org.pcsoft.app.hocal.model" }
            } catch (e: Exception) {
                null
            }
        }
        checkNotNull(own) { "The module descriptor of hocal-model was not found" }
    }

    private fun targetsOf(packageName: String): Set<String> =
        descriptor.exports().single { it.source() == packageName }.targets()

    /** The plain objects, the name rules and the errors are exported to every module. */
    @Test
    fun publicPackagesAreExportedToEveryone() {
        for (name in listOf("project", "tree", "storage")) {
            assertTrue(targetsOf("org.pcsoft.app.hocal.model.$name").isEmpty(), name)
        }
    }

    /** The operations, the storage and the media store are exported to the project manager module only. */
    @Test
    fun operationPackagesAreExportedToTheManagerOnly() {
        for (name in listOf("tree.internal", "storage.internal")) {
            assertEquals(setOf(managerModule), targetsOf("org.pcsoft.app.hocal.model.$name"), name)
        }
    }

    /** No other package is exported, so a new package is protected until someone decides otherwise. */
    @Test
    fun noOtherPackageIsExported() {
        assertEquals(
            setOf("project", "tree", "storage", "tree.internal", "storage.internal").map { "org.pcsoft.app.hocal.model.$it" }.toSet(),
            descriptor.exports().map { it.source() }.toSet(),
        )
    }
}
