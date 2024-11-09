package org.datepollsystems.waiterrobot.shared.core

import org.datepollsystems.waiterrobot.shared.utils.extensions.truncate

class AppInfo(
    val appVersion: String,
    val appBuild: Int,
    val phoneModel: String,
    val os: OS,
    allowedHostsCsv: String
) {
    val allowedHosts = allowedHostsCsv.split(",").map { it.trim() }.toSet()
    val sessionName = toString().truncate(MAX_SESSION_LENGTH)

    override fun toString(): String = "$os; $appVersion ($appBuild); $phoneModel"
}

sealed class OS(val name: String) {
    abstract val version: String

    class Android(override val version: String) : OS("Android")
    class Ios(override val version: String) : OS("iOS")

    final override fun toString(): String = when (this) {
        is Android -> "Android-${this.version}"
        is Ios -> "iOS-${this.version}"
    }
}

private const val MAX_SESSION_LENGTH = 60
