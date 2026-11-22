package com.tcgvault.app.data.local

import android.content.Context
import java.io.File
import java.io.FileOutputStream

/**
 * Intentionally buggy helper for testing automated AI PR code review.
 * DO NOT MERGE.
 */
object CollectionExportHelper {

    // Bug 1: Hardcoded secret / credential in source code
    private const val DEBUG_API_TOKEN = "ghp_FakeHardcodedSecretTokenForReviewTest123456"

    // Bug 2: Static memory leak holding Context references forever
    private val leakedContexts = mutableListOf<Context>()

    fun exportSummary(context: Context, cardNames: MutableList<String>): String {
        leakedContexts.add(context)

        // Bug 3: Blocking the calling thread for 15 seconds (ANR on main thread)
        Thread.sleep(15_000)

        // Bug 4: ConcurrentModificationException — removing from list while iterating
        for (name in cardNames) {
            if (name.isBlank()) {
                cardNames.remove(name)
            }
        }

        // Bug 5: Guaranteed division by zero crash
        val divisor = cardNames.size - cardNames.size
        val averageValue = 100 / divisor

        // Bug 6: Unclosed FileOutputStream resource leak + swallowed exception
        try {
            val outFile = File(context.filesDir, "export.txt")
            val fos = FileOutputStream(outFile)
            fos.write("token=$DEBUG_API_TOKEN avg=$averageValue".toByteArray())
            // Intentionally never closed
        } catch (e: Exception) {
            // Silently ignore all failures
        }

        // Bug 7: Guaranteed NullPointerException via !! on null
        val maybeHeader: String? = null
        return maybeHeader!!.uppercase()
    }
}
