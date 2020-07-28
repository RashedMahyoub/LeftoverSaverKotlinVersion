package com.snipertech.leftoversaver.di

import com.snipertech.leftoversaver.repository.NeedyRepository
import com.snipertech.leftoversaver.retrofit.LeftoverSaverApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object NeedyRepositoryModule {
    @Singleton
    @Provides
    fun provideMainRepository(
        retrofit: LeftoverSaverApi
    ): NeedyRepository {
        return NeedyRepository(retrofit)
    }
}














