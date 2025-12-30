package com.example.logger.presentation.navigation

object Destinations {
    const val SPLASH = "splash"
    const val DASHBOARD = "dashboard"
    const val HOME = "home"
    const val SUBMIT_STANDUP = "submit_standup"
    const val SUBMIT_CONFIRM_BASE = "submit_confirm"
    const val ARG_TS = "ts"
    const val SUBMIT_CONFIRM = "$SUBMIT_CONFIRM_BASE/{$ARG_TS}"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val ROSTER = "roster"
    const val MISSING = "missing"
    const val EXPORT = "export"

    fun submitConfirm(ts: String) = "$SUBMIT_CONFIRM_BASE/$ts"
}