package com.you.plot.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val ownerType: String,
    val createdAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
)

@Entity(tableName = "listing_items", primaryKeys = ["listingId", "itemId"])
data class ListingItemEntity(
    val listingId: Long,
    val itemId: Long,
    val orderIndex: Int = 0,
)

/** Kinds of items a Listing can hold. */
object ListingOwnerType {
    const val ROUTE = "route"
    const val PLAN = "plan"
    const val START_POINT = "start_point"
}
