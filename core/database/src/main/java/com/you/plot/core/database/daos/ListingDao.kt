package com.you.plot.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.you.plot.core.database.model.ListingEntity
import com.you.plot.core.database.model.ListingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings WHERE deletedAt IS NULL AND ownerType = :ownerType ORDER BY createdAt DESC")
    fun getListings(ownerType: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id")
    suspend fun getById(id: Long): ListingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity): Long

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListing(id: Long)

    @Query("SELECT itemId FROM listing_items WHERE listingId = :listingId ORDER BY orderIndex")
    suspend fun getItemIds(listingId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(item: ListingItemEntity)

    @Query("DELETE FROM listing_items WHERE listingId = :listingId AND itemId = :itemId")
    suspend fun removeItem(listingId: Long, itemId: Long)

    @Query("DELETE FROM listing_items WHERE listingId = :listingId")
    suspend fun clearItems(listingId: Long)
}
