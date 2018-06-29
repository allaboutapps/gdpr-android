package at.allaboutapps.gdprpolicysdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import at.allaboutapps.gdpr.GdprServiceIntent

class ServicesBroadcastReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {


    if (GdprServiceIntent.ACTION_SERVICES_CHANGED != intent.action) {
      return
    }

    val servicesEnabled = intent.getBooleanExtra(GdprServiceIntent.EXTRA_ENABLED, true)

    // <insert here disable tracking>
    // e.g. FirebaseAnalytics.setAnalyticsCollectionEnabled(servicesEnabled );

    // <insert here disable crash reporting if possible (firebase is, crashlytics isn't)>
    //
  }

}