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
 *
 * @property name Name of the project.
 * @property description Free description of the project.
 * @property uuid Unique id of the project, as text.
 * @property author Author of the project.
 * @property created Time the project was created, `null` while unknown.
 * @property modified Time of the last change, `null` while unknown.
 * @property tags Tags of the project.
 * @property formatVersion Version of the file format the project was written with.
 * @property applicationVersion Version of the application that saved the file last.
 */
data class ProjectData(
    var name: String = "",
    var description: String = "",
    var uuid: String = "",
    var author: String = "",
    var created: Instant? = null,
    var modified: Instant? = null,
    var tags: MutableList<String> = mutableListOf(),
    var formatVersion: Int = ProjectFormat.FORMAT_VERSION,
    var applicationVersion: String = "",
)
