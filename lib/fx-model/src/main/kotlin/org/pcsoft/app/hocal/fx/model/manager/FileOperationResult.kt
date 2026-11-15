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
package org.pcsoft.app.hocal.fx.model.manager

import org.pcsoft.app.hocal.model.storage.DamagedProjectFileException
import org.pcsoft.app.hocal.model.storage.InvalidProjectFileException
import org.pcsoft.app.hocal.model.storage.ManipulatedProjectFileException
import org.pcsoft.app.hocal.model.storage.PasswordRequiredException
import org.pcsoft.app.hocal.model.storage.UnsupportedEncryptionVersionException
import org.pcsoft.app.hocal.model.storage.UnsupportedFormatVersionException
import org.pcsoft.app.hocal.model.storage.WrongPasswordException
import java.io.IOException

/** What a file operation of the [ProjectManager] ended with. It never holds a password or file content. */
sealed class FileOperationResult {

    /** The operation worked. */
    object Success : FileOperationResult()

    /** Another file operation was running ([ProjectManager.busyProperty]); nothing was done and nothing changed. */
    object Busy : FileOperationResult()

    /**
     * The operation failed. The loaded project, its path and its password are as they were before.
     *
     * @property error What went wrong, in terms a user interface can show.
     * @property cause The exception behind it, for the log; its message may name the file but never a password.
     */
    class Failure(val error: FileError, val cause: Throwable) : FileOperationResult()
}

/** The reasons a file operation can fail for. */
enum class FileError {

    /** The content breaks a rule of the project format. */
    INVALID,

    /** The ZIP is broken or exceeds a limit. */
    DAMAGED,

    /** The file was written by a newer version of the application. */
    NEWER_VERSION,

    /** The password does not match. */
    WRONG_PASSWORD,

    /** The file is encrypted and no password was given. */
    PASSWORD_REQUIRED,

    /** The encrypted content failed its check: manipulated, damaged or cut off. */
    MANIPULATED,

    /** The encryption of the file is newer than this application knows. */
    UNSUPPORTED_ENCRYPTION,

    /** The file cannot be read. */
    READ_FAILED,

    /** The file cannot be written; an existing file stays untouched. */
    WRITE_FAILED,

    /** Anything else. */
    UNKNOWN,
}

/** Maps what reading or writing threw to a [FileError]; [writing] decides which one a plain I/O problem is. */
internal fun Throwable.toFileError(writing: Boolean): FileError = when (this) {
    is InvalidProjectFileException -> FileError.INVALID
    is DamagedProjectFileException -> FileError.DAMAGED
    is UnsupportedFormatVersionException -> FileError.NEWER_VERSION
    is WrongPasswordException -> FileError.WRONG_PASSWORD
    is PasswordRequiredException -> FileError.PASSWORD_REQUIRED
    is ManipulatedProjectFileException -> FileError.MANIPULATED
    is UnsupportedEncryptionVersionException -> FileError.UNSUPPORTED_ENCRYPTION
    is IOException -> if (writing) FileError.WRITE_FAILED else FileError.READ_FAILED
    else -> FileError.UNKNOWN
}
