package com.example.docmate.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom Application class for DocMate.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation,
 * serving as the application-level dependency container.
 */
@HiltAndroidApp
class DocMateApplication : Application()