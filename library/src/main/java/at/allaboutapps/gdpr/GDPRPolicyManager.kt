package at.allaboutapps.gdpr

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

/**
 * Manages the users policy settings, when it was last accepted and whether tracking is enabled.
 */
class GDPRPolicyManager private constructor(
    private val context: Context
) {

  init {
    useDefaultPolicyHolder()
  }

  /**
   * Status holder to store the timestamp of when a user accepted the ToS.
   */
  private lateinit var policyHolder: PolicyStatusHolder

  /**
   * Link to the policy to display in [PolicyActivity]. This should be used if you need to
   * change the url at runtime, otherwise you should add it to the manifest on your application tag.
   *
   *    <meta-data
   *      android:name="@string/gdpr_sdk__policy"
   *      android:value="@string/privacy_policy" />
   */
  var policyUrl: String? = null

  private val settings = GdprSettingsProvider.GdprPreferences(context)

  var servicesEnabled
    set(value) {
      if (servicesEnabled != value) {
        settings.edit()
            .putBoolean(SETTING_SERVICES_ENABLED, value)
            .apply()
        sendChangedBroadcast()
      }
    }
    get() = settings.getBoolean(SETTING_SERVICES_ENABLED, true)

  /**
   * Whether the user has seen the latest version of the privacy policy.
   * Don't forget to call [updateLatestPolicyTimestamp] or it will always return false.
   *
   * You can use [PolicyUpdateDialogFragment.show] to show information about the latest changes.
   */
  fun shouldShowPolicy(): Boolean {
    val acceptedTimestamp = policyHolder.getPolicyAcceptedTimestamp()
    if (acceptedTimestamp == 0L) {
      return true
    }
    val latestTimestamp = settings.getLong(SETTING_LATEST_POLICY_TIMESTAMP, 0L)
    return acceptedTimestamp < latestTimestamp
  }

  /**
   * @return An Intent to show the applications privacy policy / terms.
   */
  fun getPolicyIntent(): Intent {
    return PolicyActivity.newIntent(context, policyUrl)
  }

  /**
   * Set the timestamp of the latest policy update. When this date is after the time the user last accepted the policy
   * [shouldShowPolicy] will return true.
   */
  fun updateLatestPolicyTimestamp(timestamp: Long) {
    settings.edit().putLong(SETTING_LATEST_POLICY_TIMESTAMP, timestamp).apply()
  }

  /**
   * Mark the policy as accepted with the current timestamp, or reset it with `false`.
   *
   * This should primarily be used with local apps. Use [setPolicyAccepted] to update the timestamp with values from the server.
   */
  fun setPolicyAccepted(isAccepted: Boolean) {
    val timestamp = if (isAccepted) System.currentTimeMillis() else 0L
    setPolicyAccepted(timestamp)
  }

  /**
   * Update the timestamp when the policy was accepted. This should be set after a user successfully logged in.
   *
   * Alternatively you can use [setPolicyAccepted] for local apps.
   */
  fun setPolicyAccepted(timestamp: Long) {
    if (policyHolder.getPolicyAcceptedTimestamp() != timestamp) {
      policyHolder.setPolicyAcceptedTimestamp(timestamp)
      sendAcceptedBroadcast()
    }
  }

  /**
   * Fetch the timestamp when the policy was accepted.
   *
   * @return the timestamp, or `0` if it was not set.
   */
  fun getPolicyAccepted(): Long {
    return policyHolder.getPolicyAcceptedTimestamp()
  }

  /**
   * Store policy information in SharedPreferences. Alternatively you can use [policyHolder] to set your own.
   */
  private fun useDefaultPolicyHolder() {
    policyHolder = PreferencePolicyStatusHolder()
  }

  private fun sendAcceptedBroadcast() {
    val intent = Intent(GdprServiceIntent.ACTION_POLICY_ACCEPTED)
    sendBroadcast(intent)
  }

  private fun sendChangedBroadcast() {
    val intent = Intent(GdprServiceIntent.ACTION_SERVICES_CHANGED)
    sendBroadcast(intent)
  }

  private fun sendBroadcast(intent: Intent) {
    intent.setPackage(context.packageName) // limit to current app!
        .putExtra(GdprServiceIntent.EXTRA_TIMESTAMP, policyHolder.getPolicyAcceptedTimestamp())
        .putExtra(GdprServiceIntent.EXTRA_ENABLED, servicesEnabled)

    val packageName = context.packageName
    val broadcastReceivers = context.packageManager.queryBroadcastReceivers(intent, 0)

    broadcastReceivers.forEach {
      intent.setClassName(packageName, it.activityInfo.name)
      context.sendBroadcast(intent)
    }
  }


  inner class PreferencePolicyStatusHolder : PolicyStatusHolder {

    override fun setPolicyAcceptedTimestamp(timestamp: Long) {
      settings.modify {
        if (timestamp <= 0L) {
          remove(SETTING_ACCEPTED_AT)
        } else {
          putLong(SETTING_ACCEPTED_AT, timestamp)
        }
      }
    }

    override fun getPolicyAcceptedTimestamp(): Long {
      return settings.getLong(SETTING_ACCEPTED_AT, 0)
    }
  }

  @SuppressLint("ApplySharedPref")
  private inline fun SharedPreferences.modify(block: SharedPreferences.Editor.() -> Unit): SharedPreferences {
    val editor = edit()
    block(editor)
    editor.commit() // make sure write finishes
    return this
  }

  companion object {

    private const val SETTING_ACCEPTED_AT = "accepted_at"

    private const val SETTING_LATEST_POLICY_TIMESTAMP = "latest_policy_timestamp"
    private const val SETTING_SERVICES_ENABLED = "services_enabled"

    @SuppressLint("StaticFieldLeak")
    private var instance: GDPRPolicyManager? = null

    /**
     * Initialize [GDPRPolicyManager] for the first use. Best called from your applications `onCreate`.
     *
     * @see android.app.Application.onCreate
     */
    @JvmStatic
    @Synchronized
    fun initialize(context: Context): GDPRPolicyManager {
      if (instance == null) {
        instance = GDPRPolicyManager(context.applicationContext)
        GdprSettingsProvider.initialize(context)
      }
      return instance()
    }

    /**
     * Fetch the instance of [GDPRPolicyManager].
     *
     * @return the singleton instance
     */
    @JvmStatic
    fun instance(): GDPRPolicyManager {
      return this.instance
          ?: throw IllegalStateException("Please call `initialize(Context)` before using this.")
    }

  }
}