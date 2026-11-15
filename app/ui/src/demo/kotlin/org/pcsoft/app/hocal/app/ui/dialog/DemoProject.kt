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

package org.pcsoft.app.hocal.app.ui.dialog

import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.pcsoft.app.hocal.model.project.VirtualNode

/**
 * The sample project of the demo: a root folder with two subfolders and a handful of image entries.
 */
object DemoProject {

    /**
     * Builds a new sample project.
     *
     * @return the root folder of the sample project
     */
    fun create(): VirtualFolder =
        folder(
            "Sample project",
            image("Cover.png", "demo-0"),
            folder(
                "Landscapes",
                image("Mountain.png", "demo-1"),
                image("Lake.png", "demo-2"),
                image("Forest.png", "demo-3"),
            ),
            folder(
                "Portraits",
                image("Woman.png", "demo-4"),
                image("Man.png", "demo-5"),
                folder("Archive", image("Old portrait.png", "demo-6")),
            ),
        )

    private fun folder(name: String, vararg children: VirtualNode): VirtualFolder =
        VirtualFolder().apply {
            this.name = name
            this.children = children.toMutableList()
        }

    private fun image(name: String, reference: String): VirtualEntry =
        VirtualEntry().apply {
            this.name = name
            this.reference = reference
        }
}
