package at.allaboutapps.gdprpolicysdk.services

import timber.log.Timber

class DemoTrackerHandler :
    TrackerHandler {
    override fun setTracking(enabled: Boolean) {
        Timber.d("Set enabled $enabled")
    }

    override fun clearData() {
        Timber.d("Clear data")
    }
}
