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
        val serviceId = intent.getIntExtra(GdprServiceIntent.EXTRA_SERVICE, 0)
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
            val serviceIdName = context.resources.getResourceEntryName(serviceId)
            Timber.d("  $serviceIdName($serviceId): ${if (enabled) "enabled" else "disabled"}")
            getTrackerHandler(serviceId).setTracking(enabled)
        }
    }

    private fun getTrackerHandler(serviceId: Int): TrackerHandler =
        when (serviceId) {
            R.id.gdpr_service_crashlytics -> CrashlyticsTrackerHandler()
            R.id.gdpr_service_firebase -> FirebaseTrackerHandler(context)
            R.id.gdpr_service_demo -> DemoTrackerHandler()
            else -> error("unknown service id $serviceId")
        }
}
