package at.allaboutapps.gdpr

/**
 * Stores the timestamp of when a user accepted the ToS.
 */
internal interface PolicyStatusHolder {
    /**
     * Timestamp of when the policy was accepted.
     * @return `0` for unknown
     */
    fun getPolicyAcceptedTimestamp(): Long

    /**
     * Update the timestamp when the policy was accepted. `0` or negative values will be treated as not accepted.
     */
    fun setPolicyAcceptedTimestamp(timestamp: Long)
}
