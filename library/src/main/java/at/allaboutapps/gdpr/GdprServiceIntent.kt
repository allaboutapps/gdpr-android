package at.allaboutapps.gdpr

object GdprServiceIntent {
    /**
     * The user changed the services settings. [EXTRA_SERVICE] along with [EXTRA_ENABLED] and
     * [EXTRA_CLEAR] might be set if only a single service was changed, otherwise the extras will be empty.
     */
    const val ACTION_SERVICES_CHANGED = "${BuildConfig.LIBRARY_PACKAGE_NAME}.SERVICES_CHANGED"

    /**
     * The latest policy was accepted. Includes [EXTRA_TIMESTAMP] with the date. `0` indicates _not accepted_.
     */
    const val ACTION_POLICY_ACCEPTED = "${BuildConfig.LIBRARY_PACKAGE_NAME}.POLICY_ACCEPTED"

    /**
     * Current status for tracking and analytics services.
     */
    const val EXTRA_SERVICE = "service_id"

    /**
     * Current tracking status of a service. Use [EXTRA_SERVICE] to get the service id.
     */
    const val EXTRA_ENABLED = "service_enabled"

    /**
     * Whether the data should be cleared (if possible). Use [EXTRA_SERVICE] to get the service id.
     */
    const val EXTRA_CLEAR = "clear_data"

    /**
     * UTC timestamp.
     */
    const val EXTRA_TIMESTAMP = "timestamp"
}
