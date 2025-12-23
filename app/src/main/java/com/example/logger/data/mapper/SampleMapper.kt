package com.example.logger.data.mapper

import com.example.logger.data.remote.dto.SampleDto
import com.example.logger.domain.model.Sample

fun SampleDto.toDomain(): Sample = Sample(
    id = id.orEmpty(),
    title = title.orEmpty(),
    imageUrl = imageUrl
)

