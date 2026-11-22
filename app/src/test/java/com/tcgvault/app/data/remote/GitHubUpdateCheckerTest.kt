package com.tcgvault.app.data.remote

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubUpdateCheckerTest {

    @Test
    fun isNewerVersion_comparesSemanticVersionsAccurately() {
        assertTrue(GitHubUpdateChecker.isNewerVersion("v1.0.1", "1.0.0"))
        assertTrue(GitHubUpdateChecker.isNewerVersion("1.2.0", "1.1.9"))
        assertTrue(GitHubUpdateChecker.isNewerVersion("v2.0", "1.9.9"))
        assertTrue(GitHubUpdateChecker.isNewerVersion("v1.10.0", "1.2.0"))

        assertFalse(GitHubUpdateChecker.isNewerVersion("v1.0.0", "1.0.0"))
        assertFalse(GitHubUpdateChecker.isNewerVersion("1.0.0", "1.0.1"))
        assertFalse(GitHubUpdateChecker.isNewerVersion("v0.9.9", "1.0.0"))
        assertFalse(GitHubUpdateChecker.isNewerVersion("", "1.0.0"))
    }
}
