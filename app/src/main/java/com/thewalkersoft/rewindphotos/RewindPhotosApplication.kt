package com.thewalkersoft.rewindphotos

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Rewind Photos.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class RewindPhotosApplication : Application()

