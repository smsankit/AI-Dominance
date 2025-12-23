package com.example.logger.core.network

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MockStandupJson {
    fun today(): String {
        val date = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) } catch (_: Throwable) { "2025-12-22" }
        // Matches StandupResponseDto shape
        return """
        {
          "date": "$date",
          "roster": ["Alex Johnson","Priya Verma","Miguel Santos","Sarah Kim","You"],
          "submissions": [
            { "id":"s1", "name":"Alex Johnson", "yesterday":"Reviewed PRs", "today":"Finalize API spec", "blockers":null, "time":"09:10", "editedAt":null },
            { "id":"s2", "name":"Priya Verma", "yesterday":"Auth flow fixes", "today":"Add MFA", "blockers":"Waiting on UX", "time":"09:25", "editedAt":null },
            { "id":"s3", "name":"Miguel Santos", "yesterday":"Telemetry", "today":"Dashboards", "blockers":"", "time":"09:40", "editedAt":null },
            { "id":"me", "name":"You", "yesterday":"Feature A tests", "today":"Implement Feature B", "blockers":"", "time":"09:55", "editedAt":"10:30" }
          ]
        }
        """.trimIndent()
    }
}
