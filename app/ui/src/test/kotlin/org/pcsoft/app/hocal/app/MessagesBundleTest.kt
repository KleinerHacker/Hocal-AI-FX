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

package org.pcsoft.app.hocal.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Properties

/**
 * Developer tests for the message bundles backing the I18N of the UI, and for [Messages].
 *
 * The bundles are read straight from the classpath instead of through [java.util.ResourceBundle], so
 * a locale never falls back to another file and each translation is checked on its own.
 */
class MessagesBundleTest {

    private fun properties(fileName: String): Properties {
        val stream = requireNotNull(javaClass.getResourceAsStream("/messages/$fileName")) {
            "bundle not found: $fileName"
        }
        return Properties().apply { stream.reader(Charsets.UTF_8).use { load(it) } }
    }

    private val base get() = properties("bundle.properties")
    private val german get() = properties("bundle_de.properties")

    /**
     * Use case: the German translation carries exactly the keys of the English base bundle, so no
     * UI text falls back to English and no unused key survives a rename.
     */
    @Test
    fun germanTranslationMirrorsTheBaseBundle() {
        assertEquals(
            base.stringPropertyNames().sorted(),
            german.stringPropertyNames().sorted(),
            "German bundle and base bundle must carry the same keys"
        )
    }

    /**
     * Use case: no bundle entry is empty, so every menu entry and hint shows a readable text.
     */
    @Test
    fun noTranslationIsEmpty() {
        listOf("bundle.properties" to base, "bundle_de.properties" to german).forEach { (name, bundle) ->
            bundle.stringPropertyNames().forEach { key ->
                assertTrue(bundle.getProperty(key).isNotBlank(), "key $key is empty in $name")
            }
        }
    }

    /**
     * Use case: the bundles are stored as UTF-8, so no text carries the replacement character of a
     * wrongly decoded file.
     */
    @Test
    fun bundlesAreDecodedAsUtf8() {
        listOf("bundle.properties" to base, "bundle_de.properties" to german).forEach { (name, bundle) ->
            bundle.stringPropertyNames().forEach { key ->
                assertFalse(bundle.getProperty(key).contains('�'), "key $key of $name is not valid UTF-8")
            }
        }
    }

    /**
     * Use case: every translation keeps the placeholders of the English text, so a formatted message
     * shows its argument in every language.
     */
    @Test
    fun translationsKeepThePlaceholders() {
        val placeholder = Regex("\\{\\d+}")
        base.stringPropertyNames().forEach { key ->
            assertEquals(
                placeholder.findAll(base.getProperty(key)).map { it.value }.toSet(),
                placeholder.findAll(german.getProperty(key)).map { it.value }.toSet(),
                "placeholders of $key differ between the bundles"
            )
        }
    }

    /**
     * Use case: the application reads a text through [Messages], which answers from the bundle of the
     * current default locale.
     */
    @Test
    fun messagesReadsFromTheBundle() {
        assertEquals("messages.bundle", Messages.BUNDLE_NAME)
        assertEquals(Messages.bundle.getString("window.main.title"), Messages["window.main.title"])
    }
}
