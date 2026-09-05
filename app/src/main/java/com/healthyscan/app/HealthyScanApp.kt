package com.healthyscan.app

import android.app.Application
import com.healthyscan.app.data.repository.ProductRepository
import com.healthyscan.app.data.repository.SettingsRepository

class HealthyScanApp : Application() {

    // Simple manual service locator. Swap for Hilt/Koin once the project grows
    // past what's comfortable to wire by hand.
    lateinit var productRepository: ProductRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        productRepository = ProductRepository(this)
        settingsRepository = SettingsRepository(this)
    }
}
