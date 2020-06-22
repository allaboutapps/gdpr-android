package at.allaboutapps.gdprpolicysdk.services

class CrashlyticsTrackerHandler() : TrackerHandler {

    override fun setTracking(enabled: Boolean) {
        // needs google services config to work
        // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
    }
}
