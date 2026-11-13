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
package org.pcsoft.app.hocal.model.storage.internal

import org.pcsoft.app.hocal.model.storage.WrongPasswordException

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

/**
 * Regression test: a fixed encrypted file, produced independently of the application by a script
 * following the format, is read and compared with its unencrypted counterpart.
 */
class ProjectStorageEncryptionRT {

    private fun resource(name: String): ByteArray =
        checkNotNull(javaClass.getResourceAsStream("/org/pcsoft/app/hocal/model/storage/$name")).use {
            it.readAllBytes()
        }

    /** The fixed encrypted file yields the same project and media as the fixed plain ZIP. */
    @Test
    fun readsTheFixedEncryptedFile() {
        val plainSink = MemoryMedia()
        val plain = ProjectStorage.read(ByteArrayInputStream(resource("sample-project.zip")), plainSink)

        val sink = MemoryMedia()
        val project = ProjectStorage.read(
            ByteArrayInputStream(resource("sample-project-encrypted.bin")), sink, StorageLimits(), "golden-password".toCharArray(),
        )

        assertEquals(plain, project)
        assertEquals(plainSink.extensions, sink.extensions)
        plainSink.contents.forEach { (key, bytes) -> assertEquals(String(bytes), String(sink.contents.getValue(key))) }
    }

    /** The fixed file rejects another password. */
    @Test
    fun fixedFileRejectsWrongPassword() {
        assertThrows(WrongPasswordException::class.java) {
            ProjectStorage.read(
                ByteArrayInputStream(resource("sample-project-encrypted.bin")), MemoryMedia(), StorageLimits(), "other".toCharArray(),
            )
        }
    }
}
