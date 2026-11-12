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

package org.pcsoft.app.hocal.model.project

/**
 * The root of `project.json`: the project data, the references with their history details and the
 * virtual directory structure.
 */
class ProjectFile {

    /** The general data of the project. */
    var project: ProjectData = ProjectData()

    /** The references of the project. */
    var references: MutableList<Reference> = mutableListOf()

    /** The root folder of the virtual directory structure. */
    var tree: VirtualNode = VirtualNode()
}
