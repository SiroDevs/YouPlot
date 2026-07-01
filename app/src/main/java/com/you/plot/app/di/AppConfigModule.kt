package com.you.plot.app.di

import com.you.plot.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Provides
    @Named("osm_user_agent")
    fun provideOsmUserAgent(): String = BuildConfig.OsmUserAgent

    @Provides
    @Named("paystack_secret")
    fun providePaystackSecret(): String = BuildConfig.PaystackSecret
}
