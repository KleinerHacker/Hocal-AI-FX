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

package org.pcsoft.app.hocal.app.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests of the notification about changed media.
 */
class MediaChangesTest {

    /**
     * Use case: a listener is registered and media of a reference change. The listener is told which
     * reference it was.
     */
    @Test
    fun listenerReceivesTheReference() {
        val received = mutableListOf<String>()
        MediaChanges.subscribe { received.add(it) }.use {
            MediaChanges.changed("r1")
            MediaChanges.changed("r2")
        }

        assertEquals(listOf("r1", "r2"), received)
    }

    /**
     * Use case: the handle of a registration is closed. The listener is not told about later changes.
     */
    @Test
    fun closedListenerIsNotToldAgain() {
        val received = mutableListOf<String>()
        val subscription = MediaChanges.subscribe { received.add(it) }
        MediaChanges.changed("before")

        subscription.close()
        MediaChanges.changed("after")

        assertEquals(listOf("before"), received)
    }

    /**
     * Use case: several parts of the window listen at once. Each of them is told about every change, and
     * closing one registration leaves the others in place.
     */
    @Test
    fun everyListenerIsTold() {
        val first = mutableListOf<String>()
        val second = mutableListOf<String>()
        val firstSubscription = MediaChanges.subscribe { first.add(it) }
        val secondSubscription = MediaChanges.subscribe { second.add(it) }

        MediaChanges.changed("r1")
        firstSubscription.close()
        MediaChanges.changed("r2")
        secondSubscription.close()

        assertEquals(listOf("r1"), first)
        assertEquals(listOf("r1", "r2"), second)
        assertTrue(first.size < second.size)
    }

    /**
     * Use case: nobody listens. Reporting a change is harmless.
     */
    @Test
    fun changeWithoutListenerIsHarmless() {
        MediaChanges.changed("nobody-listens")
    }
}
