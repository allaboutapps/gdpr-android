package at.allaboutapps.gdpr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import at.allaboutapps.gdpr.policy.PolicyFragment
import at.allaboutapps.gdpr.services.TextResource

public class GDPRActivity : AppCompatActivity() {

    private var tosMode: Int = SettingsViewModel.TOS_HIDE
    private var requireToS: Boolean = false
    private var servicesResId: Int = 0

    private lateinit var policyUrl: String
    private lateinit var termsOfServiceUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verifySetup()

        supportFragmentManager.registerFragmentLifecycleCallbacks(UpdateTitleCallback(this), false)

        val showSettings = intent.hasExtra(EXTRA_SETTINGS)

        val isConfirmResultMode = showSettings && callingActivity != null
        if (isConfirmResultMode) {
            setContentView(R.layout.gdpr_sdk__activity_result)
        } else {
            setContentView(R.layout.gdpr_sdk__activity_default)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        tosMode = intent.getIntExtra(EXTRA_REQUIRE_TOS, SettingsViewModel.TOS_HIDE)
        requireToS = tosMode == SettingsViewModel.TOS_CONFIRM

        if (savedInstanceState == null) {
            val fragment = if (showSettings) {
                loadSettings()
            } else {
                loadPolicy()
            }
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }

        if (isConfirmResultMode) {
            setupConfirmation(requireToS)
        }
    }

    private fun loadPolicy(): Fragment {
        val policy = intent.getIntExtra(EXTRA_POLICY_TYPE, POLICY_PRIVACY)

        val showSettings = intent.getBooleanExtra(EXTRA_SHOW_SETTINGS, true)
        return when (policy) {
            POLICY_PRIVACY -> {
                val title = TextResource(R.string.gdpr_sdk__policy_privacy_title)
                PolicyFragment.newInstance(title, policyUrl, showSettings)
            }
            POLICY_TOS -> {
                val title = TextResource(R.string.gdpr_sdk__policy_tos_title)
                PolicyFragment.newInstance(title, termsOfServiceUrl, showSettings)
            }
            else -> error("unknown")
        }
    }

    private fun loadSettings(): SettingsFragment {
        val showSettings = intent.getBooleanExtra(EXTRA_SHOW_SETTINGS, true)

        return SettingsFragment.newInstance(servicesResId, tosMode, showSettings)
    }

    private fun setupConfirmation(requireToS: Boolean) {
        val buttonConfirm = findViewById<Button>(R.id.action_submit)

        val factory = ViewModelFactory(servicesResId, this)
        val viewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)

        if (requireToS) {
            buttonConfirm.isEnabled = (viewModel.tosAccepted.value ?: false)
            viewModel.tosAccepted.observe(this) { accepted ->
                buttonConfirm.isEnabled = accepted
            }
        } else {
            buttonConfirm.isEnabled = true
        }
        buttonConfirm.setOnClickListener {
            GDPRPolicyManager.instance().setPolicyAccepted(true)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //region setup
    private fun verifySetup() {
        val metaBundle = loadMetaDataFromManifest()

        readServicesResId(metaBundle)
        val manager = GDPRPolicyManager.instance()
        policyUrl = readPolicyUrl(metaBundle, manager.policyUrl, R.string.gdpr_sdk__policy)
        termsOfServiceUrl =
            readPolicyUrl(metaBundle, manager.termsOfServiceUrl, R.string.gdpr_sdk__tos)
    }

    private fun loadMetaDataFromManifest(): Bundle {
        val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return ai.metaData
    }

    private fun readServicesResId(metaBundle: Bundle): Bundle {
        servicesResId = metaBundle.getInt(getString(R.string.gdpr_sdk__services), 0)

        if (servicesResId == 0) {
            throw IllegalStateException(
                "Please specify " +
                    "`<meta-data android:name=\"@string/gdpr_sdk__services\" android:resource=\"@xml/services\"/>`" +
                    " in your manifest."
            )
        }
        return metaBundle
    }

    private fun readPolicyUrl(
        metaBundle: Bundle,
        fallbackUrl: String?,
        @StringRes resId: Int
    ): String {
        return intent.getStringExtra(EXTRA_URL)
            ?: fallbackUrl
            ?: metaBundle.getString(getString(resId), null)
            ?: throw IllegalStateException(
                "No policy url set. Please specify " +
                    "`<meta-data android:name=\"@string/${resources.getResourceName(resId)}\" android:value=\"@string/my_url\" />`" +
                    " in your manifest, or use GDPRPolicyManager.policyUrl and GDPRPolicyManager.termsOfServiceUrl to set them at runtime."
            )
    }
    //endregion

    internal companion object {
        private const val EXTRA_SETTINGS = "settings"
        private const val EXTRA_URL = "url"
        private const val EXTRA_POLICY_TYPE = "policy"
        private const val EXTRA_REQUIRE_TOS = "require_tos"
        private const val EXTRA_SHOW_SETTINGS = "show_settings"

        private const val POLICY_TOS = 1
        private const val POLICY_PRIVACY = 2

        /**
         * Intent for a first app start to have the user agree to ToS and/or modify their tracking settings.
         * At least one of the Boolean flags MUST be set to `true`.
         *
         * @param context the context for the intent
         * @param requireToS whether we should require the user to accept the ToS
         * @param showSettings whether we should show the tracking settings
         */
        internal fun newConfirmationIntent(
            context: Context,
            requireToS: Boolean,
            showSettings: Boolean
        ): Intent {
            require(requireToS || showSettings) {
                "Must show at least one setting of `requireToS` and `showSettings`"
            }
            return Intent(context, GDPRActivity::class.java)
                .putExtra(EXTRA_SETTINGS, true)
                .putExtra(
                    EXTRA_REQUIRE_TOS,
                    if (requireToS) SettingsViewModel.TOS_CONFIRM else SettingsViewModel.TOS_HIDE
                )
                .putExtra(EXTRA_SHOW_SETTINGS, showSettings)
        }

        internal fun newTOSIntent(context: Context) =
            Intent(context, GDPRActivity::class.java)
                .putExtra(EXTRA_POLICY_TYPE, POLICY_TOS)
                .putExtra(EXTRA_SHOW_SETTINGS, false)

        internal fun newPolicyIntent(context: Context, showSettings: Boolean) =
            Intent(context, GDPRActivity::class.java)
                .putExtra(EXTRA_POLICY_TYPE, POLICY_PRIVACY)
                .putExtra(EXTRA_SHOW_SETTINGS, showSettings)

        @JvmStatic
        @JvmOverloads
        internal fun newIntent(
            context: Context,
            policyUrl: String? = null,
            showToSInfo: Boolean
        ): Intent =
            Intent(context, GDPRActivity::class.java)
                .putExtra(EXTRA_SETTINGS, true)
                .putExtra(EXTRA_SHOW_SETTINGS, true)
                .putExtra(EXTRA_URL, policyUrl)
                .putExtra(
                    EXTRA_REQUIRE_TOS,
                    if (showToSInfo) SettingsViewModel.TOS_INFO else SettingsViewModel.TOS_HIDE
                )
    }
}
