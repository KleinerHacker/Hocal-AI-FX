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

/**
 * Colour schemes the application theme can be dressed in.
 *
 * A scheme is a single stylesheet that defines nothing but the palette variables of the theme. The
 * base stylesheet and every component stylesheet reference those variables only, so exchanging the
 * scheme exchanges the complete appearance.
 *
 * Adding another scheme means adding a stylesheet next to the existing ones that defines exactly the
 * same variables, plus a constant here - nothing else in the code base has to change.
 *
 * @property path resource path of the stylesheet carrying the palette of this scheme
 */
enum class HocalColorScheme(val path: String) {

    /** Bright surfaces with deep navy ink, the palette of the MkDocs default scheme. */
    LIGHT("/styles/color-scheme/light.css"),

    /** Dark navy surfaces with bright ink, the palette of the MkDocs "slate" scheme. */
    DARK("/styles/color-scheme/dark.css")
}
