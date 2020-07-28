package com.snipertech.leftoversaver.di

import com.snipertech.leftoversaver.repository.VolunteerRepository
import com.snipertech.leftoversaver.retrofit.LeftoverSaverApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object VolunteerRepositoryModule {
    @Singleton
    @Provides
    fun provideMainRepository(
        retrofit: LeftoverSaverApi
    ): VolunteerRepository {
        return VolunteerRepository(retrofit)
    }
}














