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

import javafx.application.ColorScheme
import javafx.application.Platform
import javafx.stage.Window
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.ui.preview.FakeMediaSource
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.app.ui.preview.pngBytes
import org.pcsoft.app.hocal.app.ui.prompt.FlowOption
import org.pcsoft.app.hocal.app.ui.prompt.ModelOption
import org.pcsoft.app.hocal.app.ui.prompt.ModelProvider
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Developer tests of the prompt input view model, driven without a view.
 */
class PromptInputViewModelTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val viewModel = PromptInputViewModel()
    private val source = FakeMediaSource()

    @AfterEach
    fun closeLoader() {
        viewModel.thumbnails.close()
    }

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    private fun galleryEntry(name: String, reference: String): VirtualEntry = entry(name).apply { this.reference = reference }

    private fun chooses(vararg entries: VirtualEntry?) {
        val queue = ArrayDeque(entries.toList())
        viewModel.galleryOpener = { _, _, _ -> queue.removeFirst() }
    }

    private fun add() = fx { viewModel.addImage(null) }

    private fun awaitTrue(condition: () -> Boolean) {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { condition() })
    }

    private fun settle() {
        repeat(3) {
            Thread.sleep(50)
            WaitForAsyncUtils.waitForFxEvents()
        }
    }

    /**
     * Use case: the user picks two different entries in the gallery. Both appear as images in the order
     * they were added, carrying the name and the reference of the entry.
     */
    @Test
    fun imagesAreAddedInOrder() {
        chooses(galleryEntry("Sunrise.png", "r1"), galleryEntry("Sunset.png", "r2"))

        add()
        add()

        assertEquals(listOf(PromptImage("r1", "Sunrise.png"), PromptImage("r2", "Sunset.png")), viewModel.images.toList())
    }

    /**
     * Use case: the user picks an entry whose reference is attached already, maybe under another name.
     * The duplicate is ignored; the identity of an image is its reference.
     */
    @Test
    fun duplicateReferenceIsIgnored() {
        chooses(galleryEntry("Sunrise.png", "r1"), galleryEntry("Link to sunrise.png", "r1"))

        add()
        add()

        assertEquals(listOf(PromptImage("r1", "Sunrise.png")), viewModel.images.toList())
    }

    /**
     * Use case: the user removes an image; the others stay in their order.
     */
    @Test
    fun imageIsRemoved() {
        chooses(galleryEntry("A.png", "r1"), galleryEntry("B.png", "r2"))
        add()
        add()

        fx { viewModel.removeImage(PromptImage("r1", "A.png")) }

        assertEquals(listOf(PromptImage("r2", "B.png")), viewModel.images.toList())
    }

    /**
     * Use case: the user closes the gallery dialog without a choice. The opener returns null and the
     * images stay as they are.
     */
    @Test
    fun cancelledDialogChangesNothing() {
        chooses(galleryEntry("A.png", "r1"), null)
        add()

        add()

        assertEquals(1, viewModel.images.size)
    }

    /**
     * Use case: the gallery is opened. The opener receives the root folder and the media source the
     * caller bound, and the owner window.
     */
    @Test
    fun galleryOpenerReceivesFolderAndSource() {
        val root: VirtualFolder = folder("Project")
        var receivedFolder: VirtualFolder? = null
        var receivedSource: MediaSource? = null
        var receivedOwner: Window? = null
        viewModel.folder.set(root)
        viewModel.mediaSource.set(source)
        viewModel.galleryOpener = { owner, folder, mediaSource ->
            receivedOwner = owner
            receivedFolder = folder
            receivedSource = mediaSource
            null
        }

        add()

        assertSame(root, receivedFolder)
        assertSame(source, receivedSource)
        assertNull(receivedOwner)
    }

    /**
     * Use case: the send button follows the prompt. It is disabled while the prompt is empty or only
     * blank, and enabled as soon as the prompt has a visible character.
     */
    @Test
    fun sendIsEnabledOnlyForANonBlankPrompt() {
        assertTrue(viewModel.sendDisabled.get())

        viewModel.prompt.set("   \t")
        assertTrue(viewModel.sendDisabled.get())

        viewModel.prompt.set("A red fox")
        assertFalse(viewModel.sendDisabled.get())

        viewModel.prompt.set("")
        assertTrue(viewModel.sendDisabled.get())
    }

    /**
     * Use case: the user sends a prompt. The send handler is called once; with a blank prompt it is not
     * called.
     */
    @Test
    fun sendCallsTheHandler() {
        var calls = 0
        viewModel.sendHandler = { calls++ }

        viewModel.send()
        assertEquals(0, calls)

        viewModel.prompt.set("A red fox")
        viewModel.send()
        assertEquals(1, calls)
    }

    /**
     * Use case: no handler is set, as sending is not implemented yet. Sending does not fail.
     */
    @Test
    fun sendWithoutHandlerIsHarmless() {
        viewModel.prompt.set("A red fox")

        viewModel.send()

        assertNull(viewModel.sendHandler)
    }

    /**
     * Use case: the component starts with the placeholder provider. The first model is chosen, and its
     * first flow with it.
     */
    @Test
    fun firstModelAndFlowAreChosenAtStart() {
        assertEquals(3, viewModel.models.size)
        assertEquals("Placeholder model 1", viewModel.selectedModel.get()?.name)
        assertEquals(3, viewModel.flows.size)
        assertEquals("Text to Image", viewModel.selectedFlow.get()?.name)
    }

    /**
     * Use case: the user chooses another model. The flows are those of the new model.
     */
    @Test
    fun choosingAModelReplacesTheFlows() {
        viewModel.selectedModel.set(viewModel.models[1])

        assertEquals(listOf("Text to Image", "Image to Image"), viewModel.flows.map { it.name })
    }

    /**
     * Use case: the user chose the flow "Image to Image" and changes to a model that offers it as well.
     * The chosen flow stays, recognised by its identifier.
     */
    @Test
    fun sameFlowStaysWhenTheModelChanges() {
        viewModel.selectedFlow.set(viewModel.flows[1])

        viewModel.selectedModel.set(viewModel.models[1])

        assertEquals("Image to Image", viewModel.selectedFlow.get()?.name)
    }

    /**
     * Use case: the chosen flow does not exist in the new model. The first flow of the new model is
     * chosen; a model with a single flow has it chosen.
     */
    @Test
    fun missingFlowFallsBackToTheFirstAndSingleFlowIsChosen() {
        viewModel.selectedFlow.set(viewModel.flows[2])

        viewModel.selectedModel.set(viewModel.models[2])

        assertEquals(1, viewModel.flows.size)
        assertEquals("Text to Image", viewModel.selectedFlow.get()?.name)
    }

    /**
     * Use case: another provider is set. The models are loaded anew, the first one is chosen and its
     * first flow with it.
     */
    @Test
    fun exchangingTheProviderReloadsTheModels() {
        val alpha = ModelOption("alpha", "Alpha")
        val beta = ModelOption("beta", "Beta")
        val flows = listOf(FlowOption("draw", "Draw"), FlowOption("paint", "Paint"))
        viewModel.modelProvider = object : ModelProvider {
            override fun models(): List<ModelOption> = listOf(alpha, beta)
            override fun flows(model: ModelOption): List<FlowOption> = if (model == alpha) flows else emptyList()
        }

        assertEquals(listOf(alpha, beta), viewModel.models.toList())
        assertEquals(alpha, viewModel.selectedModel.get())
        assertEquals(flows, viewModel.flows.toList())
        assertEquals(flows[0], viewModel.selectedFlow.get())
    }

    /**
     * Use case: an image is attached while a media source is bound. The preview of its current medium
     * is loaded in the background; until then it is empty.
     */
    @Test
    fun previewIsLoadedForTheAttachedImage() {
        source.add("r1", "png" to pngBytes(40, 40))
        viewModel.mediaSource.set(source)
        chooses(galleryEntry("A.png", "r1"))

        add()

        awaitTrue { fx { viewModel.previewOf("r1").get() != null } }
    }

    /**
     * Use case: the media source is exchanged, as another project was opened. The previews of the
     * attached images are loaded from the new source.
     */
    @Test
    fun exchangingTheSourceReloadsThePreviews() {
        source.add("r1", "png" to pngBytes(40, 40, 0xFF0000))
        val other = FakeMediaSource().apply { add("r1", "png" to pngBytes(40, 40, 0x0000FF)) }
        viewModel.mediaSource.set(source)
        chooses(galleryEntry("A.png", "r1"))
        add()
        awaitTrue { fx { viewModel.previewOf("r1").get() != null } }

        fx { viewModel.mediaSource.set(other) }

        awaitTrue { fx { viewModel.previewOf("r1").get()?.pixelReader?.getArgb(5, 5) == (0xFF0000FF).toInt() } }
    }

    /**
     * Use case: the media of an image changed. After the invalidation the preview is loaded again from
     * the source, although the cache held the old one.
     */
    @Test
    fun invalidateLoadsThePreviewAgain() {
        source.add("r1", "png" to pngBytes(40, 40, 0xFF0000))
        viewModel.mediaSource.set(source)
        chooses(galleryEntry("A.png", "r1"))
        add()
        awaitTrue { fx { viewModel.previewOf("r1").get() != null } }

        source.add("r1", "png" to pngBytes(40, 40, 0x00FF00))
        viewModel.invalidate("r1")

        awaitTrue { fx { viewModel.previewOf("r1").get()?.pixelReader?.getArgb(5, 5) == (0xFF00FF00).toInt() } }
    }

    /**
     * Use case: the user removes an image whose preview is still loading. The load is cancelled and the
     * late result is not delivered to the removed image.
     */
    @Test
    fun removingTheImageCancelsTheLoad() {
        source.add("r1", "png" to pngBytes(40, 40))
        source.gate = CountDownLatch(1)
        viewModel.mediaSource.set(source)
        chooses(galleryEntry("A.png", "r1"))
        add()
        awaitTrue { source.openRequested.get() >= 1 }
        val property = fx { viewModel.previewOf("r1") }

        fx { viewModel.removeImage(PromptImage("r1", "A.png")) }
        source.gate!!.countDown()
        settle()

        assertNull(property.get())
    }

    /**
     * Use case: onShow and onHide register the appearance listener symmetrically. Showing registers it
     * once, hiding removes it, and showing again registers it anew; the dark state is read on show.
     */
    @Test
    fun showAndHideAreSymmetric() {
        assertEquals(0, viewModel.appearanceRegistrations)

        fx { viewModel.onShow() }
        assertEquals(1, viewModel.appearanceRegistrations)
        assertEquals(fx { Platform.getPreferences().colorScheme == ColorScheme.DARK }, viewModel.dark.get())

        fx { viewModel.onHide() }
        assertEquals(0, viewModel.appearanceRegistrations)

        fx { viewModel.onShow() }
        assertEquals(1, viewModel.appearanceRegistrations)
        fx { viewModel.onHide() }
        assertEquals(0, viewModel.appearanceRegistrations)
    }

    /**
     * Use case: a view reports the screen state twice. A second show registers nothing more, and a
     * second hide removes nothing more.
     */
    @Test
    fun doubleShowRegistersOnce() {
        fx {
            viewModel.onShow()
            viewModel.onShow()
        }
        assertEquals(1, viewModel.appearanceRegistrations)

        fx {
            viewModel.onHide()
            viewModel.onHide()
        }
        assertEquals(0, viewModel.appearanceRegistrations)
        assertNotNull(viewModel.dark)
    }
}
