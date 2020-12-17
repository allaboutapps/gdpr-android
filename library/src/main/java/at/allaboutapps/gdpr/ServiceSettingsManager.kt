package at.allaboutapps.gdpr

import android.content.res.Resources
import at.allaboutapps.gdpr.services.Service

internal class ServiceSettingsManager(
    private val resources: Resources,
    val services: List<Service>,
    private val broadcaster: SettingsBroadcaster,
    private val settings: GdprSettingsProvider.GdprPreferences
) {

    fun enableAll() {
        services.forEach { service ->
            updateService(service.id, true)
        }
        broadcaster.sendServicesChangedBroadcast()
    }

    fun setEnabled(serviceId: Int, isEnabled: Boolean) {
        updateService(serviceId, isEnabled)
        broadcaster.sendServiceChangedBroadcast(serviceId, enabled = isEnabled)
    }

    fun getState(serviceId: Int): Boolean {
        val key = buildKey(serviceId)
        val defaultValue = !services.first { it.id == serviceId }.isOptIn
        return settings.getBoolean(key, defaultValue)
    }

    fun clearService(serviceId: Int) {
        updateService(serviceId, false)
        broadcaster.sendServiceChangedBroadcast(serviceId, enabled = false, clear = true)
    }

    private fun updateService(serviceId: Int, enabled: Boolean) {
        val key = buildKey(serviceId)
        settings.edit()
            .putBoolean(key, enabled)
            .apply()
    }

    private fun buildKey(serviceId: Int): String {
        val serviceIdName = settings.getNameForId(serviceId)
        return "${GdprSettingsProvider.SERVICE_PREFIX}$serviceIdName"
    }
}
