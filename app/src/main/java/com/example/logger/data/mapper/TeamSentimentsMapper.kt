package com.example.logger.data.mapper

import com.example.logger.data.remote.dto.TeamSentimentItemDto
import com.example.logger.domain.model.SentimentSummary
import com.example.logger.domain.model.TeamSentimentItem

fun TeamSentimentItemDto.toDomain(): TeamSentimentItem {
    return TeamSentimentItem(
        id = id,
        standupEntryId = standupEntryId,
        value = value,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun List<TeamSentimentItem>.toSummary(): SentimentSummary {
    var positive = 0
    var neutral = 0
    var negative = 0

    forEach { item ->
        when (item.value) {
            1 -> positive++
            0 -> neutral++
            -1 -> negative++
        }
    }

    return SentimentSummary(
        positive = positive,
        neutral = neutral,
        negative = negative,
        total = size
    )
}

