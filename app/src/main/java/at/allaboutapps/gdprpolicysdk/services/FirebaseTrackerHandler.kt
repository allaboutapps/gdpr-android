package at.allaboutapps.gdprpolicysdk.services

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseTrackerHandler(context: Context) :
    TrackerHandler {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun setTracking(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    override fun clearData() {
        firebaseAnalytics.resetAnalyticsData()
    }
}
