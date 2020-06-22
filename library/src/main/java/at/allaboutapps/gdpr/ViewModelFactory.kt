package at.allaboutapps.gdpr

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.allaboutapps.gdpr.services.ServicesPullParser

internal class ViewModelFactory(
    private val servicesResId: Int,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val parser = ServicesPullParser(
            context,
            servicesResId
        )
        val services = parser.parse()
        val broadcaster = SettingsBroadcaster(context)

        val settingsManager = ServiceSettingsManager(
            services,
            broadcaster,
            GdprSettingsProvider.GdprPreferences(context)
        )
        return SettingsViewModel(settingsManager) as T
    }
}
