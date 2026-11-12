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
package org.pcsoft.app.hocal.model.tree

import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.pcsoft.app.hocal.model.project.VirtualNode
import org.pcsoft.app.hocal.model.storage.MediaStore

/*
 * The operations on the virtual tree of a [ProjectFile]. Nodes are found by identity, not by value.
 * Every operation checks everything first and changes the project only if it can succeed.
 */

/**
 * Finds the folder that holds a node.
 *
 * @return the parent folder, `null` for the root.
 * @throws NodeNotFoundException if the node is not part of the tree.
 */
fun ProjectFile.parentOf(node: VirtualNode): VirtualFolder? {
    if (node === tree) return null
    val pending = ArrayDeque<VirtualFolder>()
    pending.add(tree)
    while (pending.isNotEmpty()) {
        val folder = pending.removeLast()
        for (child in folder.children) {
            if (child === node) return folder
            if (child is VirtualFolder) pending.add(child)
        }
    }
    throw NodeNotFoundException(node.name)
}

/**
 * Creates an empty folder.
 *
 * @throws InvalidNameException if the name breaks the rules of [TreeNames].
 * @throws DuplicateNameException if a sibling has this name.
 * @throws NodeNotFoundException if the parent is not part of the tree.
 */
fun ProjectFile.createFolder(parent: VirtualFolder, name: String): VirtualFolder {
    checkNewName(parent, name)
    val folder = VirtualFolder().also { it.name = name }
    parent.children.add(folder)
    return folder
}

/**
 * Creates an entry that links a reference. Several entries may link the same reference.
 *
 * @throws UnknownReferenceException if the project has no reference with this UUID.
 * @see createFolder
 */
fun ProjectFile.createEntry(parent: VirtualFolder, name: String, reference: String): VirtualEntry {
    checkNewName(parent, name)
    if (references.none { it.uuid == reference }) throw UnknownReferenceException(reference)
    val entry = VirtualEntry().also {
        it.name = name
        it.reference = reference
    }
    parent.children.add(entry)
    return entry
}

/**
 * Renames a folder or an entry. A change of upper and lower case alone is allowed.
 *
 * @throws RootNodeException for the root.
 * @throws InvalidNameException if the name breaks the rules of [TreeNames].
 * @throws DuplicateNameException if another sibling has this name.
 * @throws NodeNotFoundException if the node is not part of the tree.
 */
fun ProjectFile.rename(node: VirtualNode, name: String) {
    if (node === tree) throw RootNodeException("rename")
    TreeNames.validate(name)
    val parent = parentOf(node)!!
    checkFree(parent, name, node)
    node.name = name
}

/**
 * Moves a folder or an entry into another folder. Moving into the folder it already is in changes nothing.
 *
 * @throws RootNodeException for the root.
 * @throws TreeCycleException if a folder is moved into itself or into one of its subfolders.
 * @throws DuplicateNameException if the target has a sibling with this name.
 * @throws NodeNotFoundException if the node or the target is not part of the tree.
 */
fun ProjectFile.move(node: VirtualNode, target: VirtualFolder) {
    if (node === tree) throw RootNodeException("move")
    val parent = parentOf(node)!!
    if (target !== tree) parentOf(target)
    if (node is VirtualFolder && (target === node || node.containsFolder(target))) {
        throw TreeCycleException(node.name)
    }
    if (target === parent) return
    checkFree(target, node.name, null)
    parent.children.removeAll { it === node }
    target.children.add(node)
}

/**
 * Tells which references would lose their last entry if a node were deleted. Changes nothing; the
 * answer is the basis of a warning before [delete].
 *
 * @return the UUIDs, in the order of the reference list.
 * @throws NodeNotFoundException if the node is not part of the tree.
 */
fun ProjectFile.orphansOf(node: VirtualNode): List<String> {
    if (node !== tree) parentOf(node)
    val inside = HashSet<String>()
    node.collectReferences(inside)
    val outside = HashSet<String>()
    tree.collectReferences(outside, skip = node)
    return references.map { it.uuid }.filter { it in inside && it !in outside }
}

/**
 * Deletes a folder with everything below it, or an entry.
 *
 * Deleting never removes media by itself. If a reference would lose its last entry, nothing is
 * changed and `false` is returned, unless [removeOrphans] is set: then these references and their
 * media are removed too. [orphansOf] tells beforehand which ones that are.
 *
 * The tree and the reference list change before the media are deleted; if the store fails, only unused
 * media stay behind.
 *
 * @param store The store that holds the media.
 * @param removeOrphans Whether references without entry may be removed with their media.
 * @return `true` if the node was deleted, `false` if it was kept because of orphaned references.
 * @throws RootNodeException for the root.
 * @throws NodeNotFoundException if the node is not part of the tree.
 */
fun ProjectFile.delete(node: VirtualNode, store: MediaStore, removeOrphans: Boolean = false): Boolean {
    if (node === tree) throw RootNodeException("delete")
    val parent = parentOf(node)!!
    val orphans = orphansOf(node)
    if (orphans.isNotEmpty() && !removeOrphans) return false
    parent.children.removeAll { it === node }
    references.removeAll { it.uuid in orphans }
    orphans.forEach { store.deleteReference(it) }
    return true
}

/**
 * Removes a reference explicitly: all entries that link it, the reference and its media.
 *
 * @param store The store that holds the media.
 * @param uuid The UUID of the reference.
 * @throws UnknownReferenceException if the project has no reference with this UUID.
 */
fun ProjectFile.removeReference(store: MediaStore, uuid: String) {
    if (references.none { it.uuid == uuid }) throw UnknownReferenceException(uuid)
    val pending = ArrayDeque<VirtualFolder>()
    pending.add(tree)
    while (pending.isNotEmpty()) {
        val folder = pending.removeLast()
        folder.children.removeAll { it is VirtualEntry && it.reference == uuid }
        folder.children.filterIsInstance<VirtualFolder>().forEach { pending.add(it) }
    }
    references.removeAll { it.uuid == uuid }
    store.deleteReference(uuid)
}

private fun ProjectFile.checkNewName(parent: VirtualFolder, name: String) {
    TreeNames.validate(name)
    if (parent !== tree) parentOf(parent)
    checkFree(parent, name, null)
}

private fun checkFree(parent: VirtualFolder, name: String, ignore: VirtualNode?) {
    if (parent.children.any { it !== ignore && TreeNames.sameName(it.name, name) }) {
        throw DuplicateNameException(name)
    }
}

private fun VirtualFolder.containsFolder(candidate: VirtualFolder): Boolean {
    val pending = ArrayDeque<VirtualFolder>()
    pending.add(this)
    while (pending.isNotEmpty()) {
        val folder = pending.removeLast()
        for (child in folder.children) {
            if (child === candidate) return true
            if (child is VirtualFolder) pending.add(child)
        }
    }
    return false
}

private fun VirtualNode.collectReferences(into: MutableSet<String>, skip: VirtualNode? = null) {
    val pending = ArrayDeque<VirtualNode>()
    pending.add(this)
    while (pending.isNotEmpty()) {
        val node = pending.removeLast()
        if (node === skip) continue
        when (node) {
            is VirtualFolder -> pending.addAll(node.children)
            is VirtualEntry -> into.add(node.reference)
        }
    }
}
