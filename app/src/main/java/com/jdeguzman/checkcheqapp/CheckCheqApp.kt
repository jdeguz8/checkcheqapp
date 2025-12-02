package com.jdeguzman.checkcheqapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
/**
 * Application class for CheckCheq.
 *
 * Annotated with @HiltAndroidApp to bootstrap Hilt dependency injection.
 */
@HiltAndroidApp
class CheckCheqApp : Application()