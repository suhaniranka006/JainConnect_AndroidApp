package com.mycompany.jainconnect

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * This is the entry point for Hilt Dependency Injection.
 * @HiltAndroidApp triggers Hilt's code generation, constructing a dependency graph
 * that attaches to the application's lifecycle. It is required for Hilt to work.
 */
@HiltAndroidApp
class JainConnectApp : Application()
