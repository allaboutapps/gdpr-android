package at.allaboutapps.gdprpolicysdk.services

import android.content.Context
import android.content.Intent
import at.allaboutapps.gdpr.GDPRPolicyManager
import at.allaboutapps.gdpr.GdprServiceIntent
import at.allaboutapps.gdprpolicysdk.R
import timber.log.Timber

class ServicesManager(private val context: Context) {

    fun handleChangedServices(intent: Intent) {
        if (intent.hasExtra(GdprServiceIntent.EXTRA_SERVICE)) {
            refreshService(intent)
        } else {
            refreshAllServices()
        }
    }

    private fun refreshService(intent: Intent) {
        val serviceId = intent.getStringExtra(GdprServiceIntent.EXTRA_SERVICE)!!
        val enabled = intent.getBooleanExtra(GdprServiceIntent.EXTRA_ENABLED, false)
        val clearData = intent.getBooleanExtra(GdprServiceIntent.EXTRA_CLEAR, false)

        val handler = getTrackerHandler(serviceId)

        handler.setTracking(enabled)
        if (clearData) {
            handler.clearData()
        }
    }

    fun refreshAllServices() {
        val states = GDPRPolicyManager.instance()
            .readServiceStates(R.xml.gdpr_services)
        states.forEach { (serviceId, enabled) ->
            Timber.d("  $serviceId: ${if (enabled) "enabled" else "disabled"}")
            getTrackerHandler(serviceId).setTracking(enabled)
        }
    }

    private fun getTrackerHandler(serviceId: String): TrackerHandler =
        when (serviceId) {
            "crashlytics" -> CrashlyticsTrackerHandler()
            "firebase" -> FirebaseTrackerHandler(context)
            "demo" -> DemoTrackerHandler()
            else -> error("unknown service id $serviceId")
        }
}
