package com.example.logger.core.di

import com.example.logger.data.repository.SampleRepositoryImpl
import com.example.logger.data.repository.StandupRepositoryImpl
import com.example.logger.domain.repository.SampleRepository
import com.example.logger.domain.repository.StandupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSampleRepository(impl: SampleRepositoryImpl): SampleRepository

    @Binds
    @Singleton
    abstract fun bindStandupRepository(impl: StandupRepositoryImpl): StandupRepository
}
