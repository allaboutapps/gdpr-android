package at.allaboutapps.gdpr.services

internal data class Service(
    val id: String,
    val name: TextResource,
    val description: TextResource,
    val supportsDeletion: Boolean,
    val isOptIn: Boolean = false
)
