package com.you.plot.core.database.di

import android.content.Context
import androidx.room.Room
import com.you.plot.core.database.AppDatabase
import com.you.plot.core.database.daos.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideRouteDao(db: AppDatabase): RouteDao = db.routeDao()
    @Provides fun provideWaypointDao(db: AppDatabase): WaypointDao = db.waypointDao()
    @Provides fun providePlanDao(db: AppDatabase): PlanDao = db.planDao()
    @Provides fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()
    @Provides fun provideActivityDao(db: AppDatabase): ActivityDao = db.activityDao()
    @Provides fun provideStartPointDao(db: AppDatabase): StartPointDao = db.startPointDao()
    @Provides fun provideListingDao(db: AppDatabase): ListingDao = db.listingDao()
}
