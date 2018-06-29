package at.allaboutapps.gdpr

object GdprServiceIntent {
  /**
   * The user changed the services settings. Check [EXTRA_ENABLED] for the status.
   */
  const val ACTION_SERVICES_CHANGED = "at.allaboutapps.gdpr.SERVICES_CHANGED"

  /**
   * The latest policy was accepted. Inludes [EXTRA_TIMESTAMP] with the date. `0` indicates _not accepted_.
   */
  const val ACTION_POLICY_ACCEPTED = "at.allaboutapps.gdpr.POLICY_ACCEPTED"



  /**
   * Current status for tracking and analytics services.
   */
  const val EXTRA_ENABLED = "services_enabled"

  /**
   * UTC timestamp.
   */
  const val EXTRA_TIMESTAMP = "timestamp"
}