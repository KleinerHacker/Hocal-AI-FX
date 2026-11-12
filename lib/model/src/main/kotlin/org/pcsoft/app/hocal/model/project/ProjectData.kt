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

import java.time.Instant

/**
 * The general data of a project, the `project` part of `project.json`.
 *
 * Time stamps are UTC instants and are written in ISO-8601.
 */
class ProjectData {

    /** Name of the project. */
    var name: String = ""

    /** Free description of the project. */
    var description: String = ""

    /** Unique id of the project, as text. */
    var uuid: String = ""

    /** Author of the project. */
    var author: String = ""

    /** Time the project was created, `null` while unknown. */
    var created: Instant? = null

    /** Time of the last change, `null` while unknown. */
    var modified: Instant? = null

    /** Tags of the project. */
    var tags: MutableList<String> = mutableListOf()

    /** Version of the file format the project was written with. */
    var formatVersion: Int = ProjectFormat.FORMAT_VERSION

    /** Version of the application that saved the file last. */
    var applicationVersion: String = ""
}
