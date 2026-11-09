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

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.scene.Node

/**
 * Returns a binding that is `true` exactly while this node is really on screen.
 *
 * A node is on screen when it is visible itself, belongs to a scene, and that scene belongs to a
 * window that is being shown. The binding is stated in terms of the component instead of the window
 * on purpose: it also turns `false` when the node alone is hidden or when it is taken out of the
 * scene, and it does not care which window the node ends up in later. Neither scene nor window has
 * to exist yet, so the binding can be created while the FXML file is still being loaded.
 *
 * A component that registers itself somewhere global while it is shown - a listener of a storage,
 * a subscription, a timer - is meant to use this binding for both directions:
 *
 * ```kotlin
 * showing = pnlRoot.showingBinding()
 * showing.addListener { _, _, showing -> if (showing) subscribe() else unsubscribe() }
 * ```
 *
 * Every call creates a new binding. The caller MUST keep it in a field for as long as the component
 * lives: a binding that is only referenced locally may be garbage collected, and it would then stop
 * notifying, leaving the registration behind for good.
 */
fun Node.showingBinding(): BooleanBinding =
    Bindings.selectBoolean(sceneProperty(), "window", "showing").and(visibleProperty())
