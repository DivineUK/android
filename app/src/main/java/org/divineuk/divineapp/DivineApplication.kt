package org.divineuk.divineapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.divineuk.divineapp.ui.components.UiComponents

@HiltAndroidApp
class DivineApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: DivineApplication? = null

        fun getContext(): DivineApplication {
            return instance!!
        }

    }
}