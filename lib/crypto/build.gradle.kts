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
    `java-library`
    id("org.javamodularity.moduleplugin") version "2.0.1"
}

// Like lib/model, the tests run on the classpath; the module plugin would otherwise patch them into the module.
tasks.named<Test>("test") {
    extensions.getByType(org.javamodularity.moduleplugin.extensions.TestModuleOptions::class.java)
        .runOnClasspath = true
}
