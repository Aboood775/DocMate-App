package com.example.docmate.di

import com.example.docmate.presentation.main.booking.repository.BookingRepository
import com.example.docmate.presentation.main.booking.repository.BookingRepositoryImpl
import com.example.docmate.presentation.main.notification.repository.NotificationRepository
import com.example.docmate.presentation.main.notification.repository.NotificationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): BookingRepository
}
