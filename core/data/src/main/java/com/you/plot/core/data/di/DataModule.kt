package com.you.plot.core.data.di

import com.you.plot.core.data.impls.LocationRepoImpl
import com.you.plot.core.data.impls.PlanRepoImpl
import com.you.plot.core.data.impls.RouteRepoImpl
import com.you.plot.core.data.impls.ActivityRepoImpl
import com.you.plot.core.domain.repos.LocationRepo
import com.you.plot.core.domain.repos.PlanRepo
import com.you.plot.core.domain.repos.RouteRepo
import com.you.plot.core.domain.repos.ActivityRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds @Singleton
    abstract fun bindRouteRepo(impl: RouteRepoImpl): RouteRepo

    @Binds @Singleton
    abstract fun bindPlanRepo(impl: PlanRepoImpl): PlanRepo

    @Binds @Singleton
    abstract fun bindActivityRepo(impl: ActivityRepoImpl): ActivityRepo

    @Binds @Singleton
    abstract fun bindLocationRepo(impl: LocationRepoImpl): LocationRepo
}
