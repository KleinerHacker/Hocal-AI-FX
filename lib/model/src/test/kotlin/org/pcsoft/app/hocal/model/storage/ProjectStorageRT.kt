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
package org.pcsoft.app.hocal.model.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.model.storage.StorageFixtures.FIRST
import org.pcsoft.app.hocal.model.storage.StorageFixtures.SECOND
import java.io.ByteArrayInputStream
import java.time.Instant

/** Regression test: a fixed project ZIP is read, written again and read once more. */
class ProjectStorageRT {

    private fun golden(): ByteArray =
        checkNotNull(javaClass.getResourceAsStream("/org/pcsoft/app/hocal/model/storage/sample-project.zip")).use {
            it.readAllBytes()
        }

    /** The fixed file yields the expected project data, details, tree and media content. */
    @Test
    fun readsTheFixedFile() {
        val sink = MemoryMedia()

        val project = ProjectStorage.read(ByteArrayInputStream(golden()), sink)

        assertEquals("Golden", project.project.name)
        assertEquals(Instant.parse("2026-01-02T03:04:05Z"), project.project.created)
        assertEquals(listOf("animals", "golden"), project.project.tags)
        assertEquals(listOf("a fox", "a red fox"), project.references[0].media.map { it.prompt })
        assertEquals(42L, project.references[0].media[0].seed)
        assertNull(project.references[0].media[1].seed)
        assertEquals(mapOf("steps" to "20"), project.references[0].media[0].parameters)
        assertEquals("", project.tree.name)
        assertEquals("golden-original", String(sink.contentOf(FIRST, 0)))
        assertEquals("golden-variant", String(sink.contentOf(FIRST, 1)))
        assertEquals("golden-wolf", String(sink.contentOf(SECOND, 0)))
        assertEquals("jpg", sink.extensions[MemoryMedia.key(SECOND, 0)])
    }

    /** Writing the fixed file again and reading it back changes neither the project nor any medium. */
    @Test
    fun writeKeepsTheFixedFileContent() {
        val firstSink = MemoryMedia()
        val project = ProjectStorage.read(ByteArrayInputStream(golden()), firstSink)

        val secondSink = MemoryMedia()
        val again = ProjectStorage.read(ByteArrayInputStream(StorageFixtures.save(project, firstSink)), secondSink)

        assertEquals(project, again)
        assertEquals(firstSink.extensions, secondSink.extensions)
        firstSink.contents.forEach { (key, bytes) -> assertEquals(String(bytes), String(secondSink.contents.getValue(key))) }
    }
}
