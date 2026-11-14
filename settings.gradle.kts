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

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "hocal-ai-fx"

include(":app:ui")
project(":app:ui").name = "hocal-ui"

include(":lib:crypto")
project(":lib:crypto").name = "hocal-crypto"
include(":lib:model")
project(":lib:model").name = "hocal-model"
include(":lib:thumbnail")
project(":lib:thumbnail").name = "hocal-thumbnail"
include(":lib:fx-model")
project(":lib:fx-model").name = "hocal-fx-model"
