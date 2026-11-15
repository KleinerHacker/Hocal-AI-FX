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

package org.pcsoft.app.hocal.app.ui.component

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.beans.binding.BooleanBinding
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.ui.showingBinding
import java.net.URL
import java.util.ResourceBundle

/**
 * View of the media view, living in the root pane itself (`fx:root`).
 *
 * The image fills the available space and is clipped instead of blocking the layout; the placeholder icon
 * has a fixed size in the middle. The observation of the appearance is tied to the screen state.
 */
class MediaViewView : FxmlView<MediaViewViewModel>, Initializable {
    @FXML
    private lateinit var pnlRoot: StackPane

    @FXML
    private lateinit var imgMedia: ImageView

    @FXML
    private lateinit var imgPlaceholder: ImageView

    @InjectViewModel
    private lateinit var viewModel: MediaViewViewModel

    // Kept as a field: a garbage collected binding stops notifying, and the registration would stay.
    private lateinit var showing: BooleanBinding

    /**
     * Initializes the view after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        pnlRoot.clip = Rectangle().apply {
            widthProperty().bind(pnlRoot.widthProperty())
            heightProperty().bind(pnlRoot.heightProperty())
        }

        imgMedia.fitWidthProperty().bind(pnlRoot.widthProperty())
        imgMedia.fitHeightProperty().bind(pnlRoot.heightProperty())
        imgMedia.imageProperty().bind(viewModel.image)

        imgPlaceholder.visibleProperty().bind(viewModel.showsPlaceholder)
        applyPlaceholderIcon()
        viewModel.appearance.dark.addListener { _, _, _ -> applyPlaceholderIcon() }

        showing = pnlRoot.showingBinding()
        showing.addListener { _, _, onScreen -> if (onScreen) viewModel.onShow() else viewModel.onHide() }
    }

    private fun applyPlaceholderIcon() {
        imgPlaceholder.image = HocalIcons.image(HocalIcons.MEDIA_PLACEHOLDER, PLACEHOLDER_SIZE, viewModel.appearance.dark.get())
    }

    private companion object {
        const val PLACEHOLDER_SIZE = 96
    }
}
