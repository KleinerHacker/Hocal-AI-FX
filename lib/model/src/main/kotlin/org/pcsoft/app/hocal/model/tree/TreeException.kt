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

/** Base of the errors of the virtual tree operations. */
sealed class TreeException(message: String) : Exception(message)

/**
 * A name breaks the name rules, see [TreeNames].
 *
 * @property name The rejected name.
 */
class InvalidNameException(val name: String, reason: String) :
    TreeException("Invalid name '$name': $reason")

/**
 * A sibling already has this name, upper and lower case are not told apart.
 *
 * @property name The name that is taken.
 */
class DuplicateNameException(val name: String) : TreeException("The folder already holds an item named '$name'")

/**
 * The node is not part of the tree of the project.
 *
 * @property name The name of the node.
 */
class NodeNotFoundException(val name: String) : TreeException("'$name' is not part of the tree")

/**
 * A folder cannot be moved into itself or into one of its own subfolders.
 *
 * @property name The name of the folder.
 */
class TreeCycleException(val name: String) : TreeException("Folder '$name' cannot be moved into itself")

/**
 * The root folder cannot be renamed, moved or deleted.
 *
 * @property operation The rejected operation.
 */
class RootNodeException(val operation: String) : TreeException("The root folder cannot be changed by: $operation")

/**
 * The project has no reference with this UUID.
 *
 * @property reference The UUID.
 */
class UnknownReferenceException(val reference: String) : TreeException("Unknown reference '$reference'")
