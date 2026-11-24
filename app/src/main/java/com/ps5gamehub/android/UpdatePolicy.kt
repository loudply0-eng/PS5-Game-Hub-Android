package com.ps5gamehub.android

import java.net.URL

internal object UpdatePolicy {
    private val trustedHosts = setOf(
        "github.com",
        "objects.githubusercontent.com",
        "release-assets.githubusercontent.com"
    )

    fun parseVersion(value: String): List<Int>? {
        val match = Regex("^v?(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)$")
            .matchEntire(value.trim()) ?: return null
        return match.groupValues.drop(1).map { it.toIntOrNull() ?: return null }
    }

    fun compareVersions(a: List<Int>, b: List<Int>): Int {
        for (i in 0 until maxOf(a.size, b.size)) {
            val left = a.getOrElse(i) { 0 }
            val right = b.getOrElse(i) { 0 }
            if (left != right) return left.compareTo(right)
        }
        return 0
    }

    fun trustedDownloadUrl(address: String): URL? {
        val url = runCatching { URL(address) }.getOrNull() ?: return null
        if (url.protocol != "https" || url.host.lowercase() !in trustedHosts ||
            url.userInfo != null || url.port !in setOf(-1, 443)) return null
        return url
    }
}
