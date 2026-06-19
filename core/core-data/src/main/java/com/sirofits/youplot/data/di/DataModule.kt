package com.sirofits.youplot.data.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.sirofits.youplot.data.local.YouPlotDatabase
import com.sirofits.youplot.data.local.dao.*
import com.sirofits.youplot.data.repository.*
import com.sirofits.youplot.domain.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): YouPlotDatabase =
        Room.databaseBuilder(context, YouPlotDatabase::class.java, YouPlotDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideRouteDao(db: YouPlotDatabase): RouteDao = db.routeDao()
    @Provides fun provideWaypointDao(db: YouPlotDatabase): WaypointDao = db.waypointDao()
    @Provides fun providePlanDao(db: YouPlotDatabase): PlanDao = db.planDao()
    @Provides fun providePlanEventDao(db: YouPlotDatabase): PlanEventDao = db.planEventDao()
    @Provides fun provideSessionDao(db: YouPlotDatabase): SessionDao = db.sessionDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindRouteRepository(impl: RouteRepositoryImpl): RouteRepository

    @Binds @Singleton
    abstract fun bindPlanRepository(impl: PlanRepositoryImpl): PlanRepository

    @Binds @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository
}
