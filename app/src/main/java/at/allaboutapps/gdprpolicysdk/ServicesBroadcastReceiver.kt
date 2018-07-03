package at.allaboutapps.gdprpolicysdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import at.allaboutapps.gdpr.GdprServiceIntent

class ServicesBroadcastReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {

    val action = intent.action

    when (action) {
      GdprServiceIntent.ACTION_POLICY_ACCEPTED -> {
      }
      GdprServiceIntent.ACTION_SERVICES_CHANGED -> {
      }
      else -> return
    }

    val servicesEnabled = intent.getBooleanExtra(GdprServiceIntent.EXTRA_ENABLED, true)
    val acceptedAt = intent.getLongExtra(GdprServiceIntent.EXTRA_TIMESTAMP, 0L)

    // <insert here disable tracking>
    // e.g. FirebaseAnalytics.setAnalyticsCollectionEnabled(servicesEnabled );

    // <insert here disable crash reporting if possible (firebase is, crashlytics isn't)>
  }

}