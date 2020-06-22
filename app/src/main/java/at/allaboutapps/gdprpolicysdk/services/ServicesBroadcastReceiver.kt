package at.allaboutapps.gdprpolicysdk.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import at.allaboutapps.gdpr.GdprServiceIntent
import timber.log.Timber
import java.util.Date

class ServicesBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d(intent.logToString())

        when (intent.action) {
            GdprServiceIntent.ACTION_POLICY_ACCEPTED -> {
                val acceptedAt = intent.getLongExtra(GdprServiceIntent.EXTRA_TIMESTAMP, 0L)
                Timber.d("Policy accepted at $acceptedAt (${Date(acceptedAt)})")
            }
            GdprServiceIntent.ACTION_SERVICES_CHANGED -> {
                ServicesManager(context).handleChangedServices(intent)
            }
            else -> return
        }
    }
}

private fun Intent.logToString(): String {
    val extras = extras
    val items = if (extras != null) extras.keySet()?.map { "$it: ${extras.get(it)}" } else null
    return "$action ($items)"
}
