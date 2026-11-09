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

import java.util.ResourceBundle

/**
 * Access to the translated texts of the user interface.
 *
 * The bundle follows the default locale of the system; English is the base translation, German is
 * shipped alongside it.
 */
object Messages {

    /** Base name of the resource bundle holding every UI text. */
    const val BUNDLE_NAME: String = "messages.bundle"

    /** The bundle for the current default locale. */
    val bundle: ResourceBundle by lazy { ResourceBundle.getBundle(BUNDLE_NAME) }

    /**
     * Reads a translated text.
     *
     * @param key key of the text inside the bundle
     * @return the translated text
     */
    operator fun get(key: String): String = bundle.getString(key)
}
