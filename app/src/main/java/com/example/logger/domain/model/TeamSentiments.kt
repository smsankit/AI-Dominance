package com.example.logger.domain.model

data class TeamSentimentItem(
    val id: Long,
    val standupEntryId: Long,
    val value: Int, // -1: Negative, 0: Neutral, 1: Positive
    val createdAt: String,
    val updatedAt: String
)

enum class SentimentType {
    POSITIVE,
    NEUTRAL,
    NEGATIVE
}

data class SentimentSummary(
    val positive: Int,
    val neutral: Int,
    val negative: Int,
    val total: Int
) {
    val dominantSentiment: SentimentType
        get() = when {
            positive >= neutral && positive >= negative -> SentimentType.POSITIVE
            negative >= neutral && negative >= positive -> SentimentType.NEGATIVE
            else -> SentimentType.NEUTRAL
        }
}

