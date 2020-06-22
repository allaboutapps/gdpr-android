package at.allaboutapps.gdprpolicysdk.services

interface TrackerHandler {
    fun setTracking(enabled: Boolean)
    fun clearData() = Unit
}
