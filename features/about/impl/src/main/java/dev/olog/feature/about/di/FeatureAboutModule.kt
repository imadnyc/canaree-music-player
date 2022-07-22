package dev.olog.feature.about.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.olog.feature.about.navigation.FeatureAboutNavigatorImpl
import dev.olog.feature.about.api.FeatureAboutNavigator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureAboutModule {

    @Binds
    @Singleton
    abstract fun provideNavigator(impl: FeatureAboutNavigatorImpl): FeatureAboutNavigator

}