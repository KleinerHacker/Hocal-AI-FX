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
import org.pcsoft.app.hocal.model.project.Medium
import org.pcsoft.app.hocal.model.project.ProjectData
import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.project.Reference
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Media held in memory; usable as source and as sink. Content and details carry the same tag. */
class MemoryMedia : MediaSource, MediaSink {

    val extensions = LinkedHashMap<String, String>()
    val contents = LinkedHashMap<String, ByteArray>()

    fun put(reference: String, number: Int, extension: String, bytes: ByteArray) {
        extensions[key(reference, number)] = extension
        contents[key(reference, number)] = bytes
    }

    fun contentOf(reference: String, number: Int): ByteArray = contents.getValue(key(reference, number))

    override fun extension(reference: String, number: Int): String = extensions.getValue(key(reference, number))

    override fun open(reference: String, number: Int): InputStream = ByteArrayInputStream(contentOf(reference, number))

    override fun accept(reference: String, number: Int, extension: String, content: InputStream) {
        put(reference, number, extension, content.readAllBytes())
    }

    companion object {
        fun key(reference: String, number: Int) = "$reference/$number"
    }
}

/** Sample projects and helpers shared by the storage tests. */
object StorageFixtures {

    const val FIRST = "6f1c1f0e-0000-4000-8000-0000000000a1"
    const val SECOND = "6f1c1f0e-0000-4000-8000-0000000000a2"

    fun medium(tag: String) = Medium(
        prompt = "prompt-$tag",
        model = "model-$tag",
        flow = "flow-$tag",
        seed = tag.hashCode().toLong(),
        parameters = linkedMapOf("steps" to "20", "tag" to tag),
    )

    fun content(tag: String): ByteArray = "content-$tag".toByteArray()

    /** Two references: the first with the media a, b, c, the second with the medium d. */
    fun project() = ProjectFile(
        project = ProjectData(
            name = "Sample",
            description = "A sample project",
            uuid = "6f1c1f0e-0000-4000-8000-000000000001",
            author = "Tester",
            created = Instant.parse("2026-01-02T03:04:05Z"),
            modified = Instant.parse("2026-02-03T04:05:06Z"),
            tags = mutableListOf("animals", "test"),
            applicationVersion = "1.0.0",
        ),
        references = mutableListOf(
            Reference(FIRST, mutableListOf(medium("a"), medium("b"), medium("c"))),
            Reference(SECOND, mutableListOf(medium("d"))),
        ),
        tree = VirtualFolder().apply {
            name = "root"
            children.add(VirtualFolder().apply {
                name = "animals"
                children.add(VirtualEntry().apply { name = "fox"; reference = FIRST })
            })
            children.add(VirtualEntry().apply { name = "wolf"; reference = SECOND })
        },
    )

    /** The media matching [project]: the content of every medium is derived from its prompt. */
    fun media(project: ProjectFile): MemoryMedia {
        val media = MemoryMedia()
        for (reference in project.references) {
            reference.media.forEachIndexed { number, medium ->
                media.put(reference.uuid, number, "png", content(tag(medium)))
            }
        }
        return media
    }

    fun tag(medium: Medium): String = medium.prompt!!.removePrefix("prompt-")

    fun save(project: ProjectFile, media: MediaSource): ByteArray =
        ByteArrayOutputStream().also { ProjectStorage.write(it, project, media) }.toByteArray()

    /** Asserts that every medium number of every reference still holds the content of its own details. */
    fun assertMatching(project: ProjectFile, media: MemoryMedia) {
        for (reference in project.references) {
            reference.media.forEachIndexed { number, medium ->
                assertEquals(String(content(tag(medium))), String(media.contentOf(reference.uuid, number)))
            }
        }
        assertEquals(project.references.sumOf { it.media.size }, media.contents.size)
    }

    /** All entries of a ZIP by name, in order. */
    fun entries(zip: ByteArray): LinkedHashMap<String, ByteArray> {
        val result = LinkedHashMap<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(zip)).use { stream ->
            while (true) {
                val entry = stream.nextEntry ?: break
                result[entry.name] = stream.readAllBytes()
            }
        }
        return result
    }

    /** A ZIP of the given entries, in the given order. */
    fun zip(entries: Map<String, ByteArray>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { stream ->
            for ((name, bytes) in entries) {
                stream.putNextEntry(ZipEntry(name))
                stream.write(bytes)
                stream.closeEntry()
            }
        }
        return out.toByteArray()
    }

    /** A minimal project.json with one reference `ref-1` holding one medium. */
    fun json(
        version: Int = 1,
        references: String = """[{"uuid":"ref-1","media":[{}]}]""",
        tree: String = """{"type":"folder","name":"","children":[]}""",
        extra: String = "",
    ): ByteArray =
        """{"project":{"formatVersion":$version},"references":$references,"tree":$tree$extra}""".toByteArray()
}
