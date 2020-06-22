package at.allaboutapps.gdpr

import android.content.Context
import android.content.Intent

internal class SettingsBroadcaster(private val context: Context) {

    fun sendServiceChangedBroadcast(serviceId: String, enabled: Boolean, clear: Boolean = false) {
        val intent = createIntent(GdprServiceIntent.ACTION_SERVICES_CHANGED)
            .putExtra(GdprServiceIntent.EXTRA_SERVICE, serviceId)
            .putExtra(GdprServiceIntent.EXTRA_ENABLED, enabled)
            .putExtra(GdprServiceIntent.EXTRA_CLEAR, clear)
        send(intent)
    }

    private fun send(intent: Intent) {
        val packageName = context.packageName
        val broadcastReceivers = context.packageManager.queryBroadcastReceivers(intent, 0)

        broadcastReceivers.forEach {
            intent.setClassName(packageName, it.activityInfo.name)
            context.sendBroadcast(intent)
        }
    }

    private fun createIntent(action: String) = Intent(action)
        .setPackage(context.packageName) // limit to current app!

    fun sendServicesChangedBroadcast() {
        val intent = createIntent(GdprServiceIntent.ACTION_SERVICES_CHANGED)
        send(intent)
    }
}
