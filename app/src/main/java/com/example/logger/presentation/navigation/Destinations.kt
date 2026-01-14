package com.example.logger.presentation.navigation

object Destinations {
    const val SPLASH = "splash"
    const val DASHBOARD_BASE = "dashboard"
    const val ARG_REFRESH = "refresh"
    const val DASHBOARD = "$DASHBOARD_BASE?$ARG_REFRESH={$ARG_REFRESH}"
    const val HOME_BASE = "home"
    const val HOME = "$HOME_BASE?$ARG_REFRESH={$ARG_REFRESH}"
    const val SUBMIT_STANDUP_BASE = "submit_standup"
    const val ARG_MEMBER_NAME = "memberName"
    const val SUBMIT_STANDUP = "$SUBMIT_STANDUP_BASE?$ARG_MEMBER_NAME={$ARG_MEMBER_NAME}"
    const val SUBMIT_CONFIRM_BASE = "submit_confirm"
    const val ARG_TS = "ts"
    const val SUBMIT_CONFIRM = "$SUBMIT_CONFIRM_BASE/{$ARG_TS}"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val ROSTER = "roster"
    const val MISSING_BASE = "missing"
    const val ARG_SOURCE = "source"
    const val SOURCE_DASHBOARD = "dashboard"
    const val SOURCE_HISTORY = "history"
    const val MISSING = "$MISSING_BASE?$ARG_SOURCE={$ARG_SOURCE}"
    const val EXPORT = "export"

    fun missing(source: String = SOURCE_DASHBOARD) = "$MISSING_BASE?$ARG_SOURCE=$source"

    // Sentiment analysis
    const val SENTIMENT_BASE = "sentiment"
    const val ARG_POS = "pos"
    const val ARG_NEU = "neu"
    const val ARG_NEG = "neg"
    const val ARG_TOTAL = "total"
    const val SENTIMENT = "$SENTIMENT_BASE?$ARG_POS={$ARG_POS}&$ARG_NEU={$ARG_NEU}&$ARG_NEG={$ARG_NEG}&$ARG_TOTAL={$ARG_TOTAL}"

    fun submitConfirm(ts: String) = "$SUBMIT_CONFIRM_BASE/$ts"
    fun dashboard(refresh: Boolean = false) = if (refresh) {
        "$DASHBOARD_BASE?$ARG_REFRESH=${System.currentTimeMillis()}"
    } else {
        DASHBOARD_BASE
    }
    fun home(refresh: Boolean = false) = if (refresh) {
        "$HOME_BASE?$ARG_REFRESH=${System.currentTimeMillis()}"
    } else {
        HOME_BASE
    }
    fun submitStandup(memberName: String? = null) = if (memberName != null) {
        "$SUBMIT_STANDUP_BASE?$ARG_MEMBER_NAME=${java.net.URLEncoder.encode(memberName, "UTF-8")}"
    } else {
        SUBMIT_STANDUP_BASE
    }

    fun sentimentRoute(pos: Int, neu: Int, neg: Int, total: Int): String =
        "$SENTIMENT_BASE?$ARG_POS=$pos&$ARG_NEU=$neu&$ARG_NEG=$neg&$ARG_TOTAL=$total"
}