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

package org.pcsoft.app.hocal.app.ui.preview

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Empties the cache that all thumbnails share before every test, so that a test never finds the image of another
 * test under the same reference. Registered for every test of the module through the extension auto-detection of
 * JUnit (`junit-platform.properties` and `META-INF/services`).
 */
class ThumbnailServiceReset : BeforeEachCallback {

    /** Empties the shared cache. */
    override fun beforeEach(context: ExtensionContext) {
        ThumbnailService.clear()
    }
}
