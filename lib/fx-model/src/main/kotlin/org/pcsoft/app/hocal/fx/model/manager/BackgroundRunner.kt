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

import javafx.concurrent.Task
import java.util.concurrent.CancellationException

/**
 * Runs the file access of the [ProjectManager] off the FX thread. The work runs on a background thread, the
 * answer is handed to [done] on the FX thread, with the failure if the work threw.
 */
internal interface BackgroundRunner {

    /** Runs [work] in the background and reports to [done] on the FX thread. */
    fun <T> run(work: () -> T, done: (Result<T>) -> Unit)
}

/** The runner of the application: a `javafx.concurrent.Task` on a daemon thread. Needs the FX toolkit. */
internal object TaskRunner : BackgroundRunner {

    override fun <T> run(work: () -> T, done: (Result<T>) -> Unit) {
        val task = object : Task<T>() {
            override fun call(): T = work()
        }
        task.setOnSucceeded { done(Result.success(task.value)) }
        task.setOnFailed { done(Result.failure(task.exception ?: IllegalStateException("The file operation failed"))) }
        task.setOnCancelled { done(Result.failure(CancellationException("The file operation was cancelled"))) }
        Thread(task, "hocal-file-operation").apply { isDaemon = true }.start()
    }
}

/** Runs the work at once on the calling thread: for the tests of this library, which have no toolkit. */
internal object SynchronousRunner : BackgroundRunner {

    override fun <T> run(work: () -> T, done: (Result<T>) -> Unit) {
        done(runCatching(work))
    }
}
