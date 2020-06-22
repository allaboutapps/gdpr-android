package at.allaboutapps.gdpr

import at.allaboutapps.gdpr.services.Service

internal class ServiceSettingsManager(
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

    fun setEnabled(serviceId: String, isEnabled: Boolean) {
        updateService(serviceId, isEnabled)
        broadcaster.sendServiceChangedBroadcast(serviceId, enabled = isEnabled)
    }

    fun getState(serviceId: String): Boolean {
        val key = buildKey(serviceId)
        val defaultValue = !services.first { it.id == serviceId }.isOptIn
        return settings.getBoolean(key, defaultValue)
    }

    fun clearService(serviceId: String) {
        updateService(serviceId, false)
        broadcaster.sendServiceChangedBroadcast(serviceId, enabled = false, clear = true)
    }

    private fun updateService(serviceId: String, enabled: Boolean) {
        val key = buildKey(serviceId)
        settings.edit()
            .putBoolean(key, enabled)
            .apply()
    }

    private fun buildKey(serviceId: String) = "${GdprSettingsProvider.SERVICE_PREFIX}$serviceId"
}
