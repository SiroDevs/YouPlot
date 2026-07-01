package com.you.plot.core.database.converter

import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.database.model.StartPointEntity
import com.you.plot.core.domain.entity.StartPoint

fun StartPoint.toEntity() = StartPointEntity(
    id = id, name = name,
    latitude = position.latitude, longitude = position.longitude,
    countryCode = countryCode, usageCount = usageCount,
    lastUsedAt = lastUsedAt, createdAt = createdAt,
    isFavorite = isFavorite, deletedAt = deletedAt,
)

fun StartPointEntity.toDomain() = StartPoint(
    id = id, name = name,
    position = LatLng(latitude, longitude),
    countryCode = countryCode, usageCount = usageCount,
    lastUsedAt = lastUsedAt, createdAt = createdAt,
    isFavorite = isFavorite, deletedAt = deletedAt,
)
