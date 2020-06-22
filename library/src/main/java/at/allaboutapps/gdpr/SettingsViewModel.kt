package at.allaboutapps.gdpr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

internal class SettingsViewModel(
    private val settingsManager: ServiceSettingsManager
) : ViewModel() {

    companion object {
        const val TOS_HIDE = 0
        const val TOS_CONFIRM = 1
        const val TOS_INFO = 2
    }

    var tosMode: Int = TOS_HIDE
    var showSettings: Boolean = false

    fun loadServiceItems() {
        val items = if (tosMode == TOS_CONFIRM)
            listOfNotNull(
                ListItem.TermsOfService,
                if (showSettings) ListItem.ServicesHeader else ListItem.PrivacyInfo
            )
        else {
            val tos = if (tosMode == TOS_INFO) ListItem.TermsOfServiceInfo else null
            listOfNotNull(tos, ListItem.ServicesHeader)
        }

        _listItems.value = if (showSettings) {
            val serviceItems = loadServices()
            items.plus(serviceItems)
        } else {
            items
        }
    }

    private fun loadServices(): List<ListItem.Service> = settingsManager.services.map { service ->
        ListItem.Service(
            service.id,
            service.name,
            service.description,
            getState(service.id),
            service.supportsDeletion
        )
    }

    private val _listItems: MutableLiveData<List<ListItem>> = MutableLiveData()
    val listItems: LiveData<List<ListItem>> = _listItems

    private val _tosAccepted: MutableLiveData<Boolean> = MutableLiveData()
    val tosAccepted: LiveData<Boolean> = _tosAccepted

    fun enableAll() {
        settingsManager.enableAll()
        loadServiceItems()
    }

    fun setEnabled(serviceId: String, isEnabled: Boolean) {
        settingsManager.setEnabled(serviceId, isEnabled)
        loadServiceItems()
    }

    fun setTOSAccepted(accepted: Boolean) {
        _tosAccepted.value = accepted
    }

    private fun getState(serviceId: String): Boolean {
        return settingsManager.getState(serviceId)
    }

    fun clearService(serviceId: String) {
        settingsManager.clearService(serviceId)
        loadServiceItems()
    }
}
