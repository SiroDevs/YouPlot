package com.you.plot.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-created list that can group any of Route/Plan/StartPoint. The concrete item
 * ids are held in [ListingItemEntity] rather than a JSON blob so lookups don't need
 * to deserialize the whole set.
 */
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
