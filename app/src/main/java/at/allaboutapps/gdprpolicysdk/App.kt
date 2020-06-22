package at.allaboutapps.gdprpolicysdk

import android.app.Application
import at.allaboutapps.gdprpolicysdk.services.ServicesManager
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        initializeTracking()
    }

    private fun initializeTracking() {
        Timber.i("Initializing Tracking")
        ServicesManager(this).refreshAllServices()
    }
}
