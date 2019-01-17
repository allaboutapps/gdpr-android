package at.allaboutapps.gdpr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.MenuItem
import at.allaboutapps.gdpr.policy.PolicyFragment

class PolicyActivity : AppCompatActivity() {

  enum class ScreenFlow {
      Standard,         // Use this when the app shows a policy screen in addition to service opt-in (e.g. revier)
      OptInStandalone,  // Offer to opt in, does not open any further screens (typically triggered by the app after a user logs in)
      OptIn,            // Offer to opt in (typically triggered explicitly by a user who has currently not opted in)
      OptOut            // Offer to opt out (typically triggered explicitly by a user who previously opted in)
  }

  internal lateinit var styledContext: Context
  private var servicesResId: Int = 0

  private var screenFlow = ScreenFlow.Standard

  private lateinit var policyUrl: String

  private lateinit var tintHelper: NavigationIconTintHelper

  private var gdprManager: GDPRPolicyManager = GDPRPolicyManager.instance()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    verifySetup()

    val screenFlowOrdinal = intent.getIntExtra(EXTRA_SCREEN_FLOW, ScreenFlow.Standard.ordinal)
    screenFlow = ScreenFlow.values()[screenFlowOrdinal]

    supportActionBar!!.setDisplayHomeAsUpEnabled(screenFlow != ScreenFlow.OptInStandalone)

    tintHelper = NavigationIconTintHelper(supportActionBar!!, styledContext)

    supportFragmentManager.addOnBackStackChangedListener { updateNavigationIcon() }

    supportFragmentManager.registerFragmentLifecycleCallbacks(UpdateTitleCallback(this), false)

    updateNavigationIcon()

    if (savedInstanceState == null) {
      val fragment = resolveStartFragment()

      supportFragmentManager
        .beginTransaction()
        .replace(android.R.id.content, fragment)
        .commit()
    }
  }

  private fun updateColors(fragment: BasePolicyFragment) {
    val styledContext = fragment.styledContext.value
    val typedValue = TypedValue()
    val styledTheme = styledContext.theme
    val colorPrimary = typedValue.loadAttr(styledTheme, R.attr.colorPrimary).data
    val colorPrimaryDark = typedValue.loadAttr(styledTheme, R.attr.colorPrimaryDark).data
    setToolbarColors(colorPrimary, colorPrimaryDark)
  }

  private fun resolveStartFragment(): BasePolicyFragment {
    return when (screenFlow) {
      ScreenFlow.OptInStandalone, ScreenFlow.OptIn -> StandaloneOptInFragment.newInstance(servicesResId)
      ScreenFlow.OptOut -> OptOutWarningFragment.newInstance(servicesResId)
      else -> PolicyFragment.newInstance(policyUrl)
    }
  }

  fun onShowOptOutSettingsSelected() {
    val fragment = if (areServicesEnabled()) {
      OptOutWarningFragment.newInstance(servicesResId)
    } else {
      OptInFragment.newInstance(servicesResId)
    }

    addSettingsFragment(fragment)
  }

  private fun addSettingsFragment(fragment: BasePolicyFragment) {
    supportFragmentManager.popBackStack(TAG_SETTINGS, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    supportFragmentManager.beginTransaction()
      .replace(android.R.id.content, fragment)
      //.addToBackStack(TAG_SETTINGS)
      .commit()
  }

  private fun setToolbarColors(color: Int, colorDark: Int) {
    supportActionBar!!.setBackgroundDrawable(ColorDrawable(color))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.statusBarColor = colorDark
    }
  }

  private fun areServicesEnabled(): Boolean {
    return gdprManager.servicesEnabled
  }

  override fun onAttachFragment(fragment: Fragment?) {
    super.onAttachFragment(fragment)
    if(fragment is BasePolicyFragment) {
      updateColors(fragment)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  private fun updateNavigationIcon() {
    val canGoBack = supportFragmentManager.backStackEntryCount == 0
    tintHelper.updateNavigationIcon(canGoBack)
  }

  // region callbacks
  fun onDisableServicesClicked() {
    gdprManager.servicesEnabled = false

    if (screenFlow == ScreenFlow.OptInStandalone) {
      setResult(Activity.RESULT_OK)
      finish()
      return
    }
    onShowOptOutSettingsSelected()
  }

  fun onCloseClicked() {
    setResult(Activity.RESULT_CANCELED)
    finish()
  }

  fun onEnableServicesClicked() {
    gdprManager.servicesEnabled = true

    if (screenFlow == ScreenFlow.OptInStandalone) {
      setResult(Activity.RESULT_OK)
      finish()
      return
    }

    addSettingsFragment(InformationFragment.newInstance(servicesResId))
  }
  //endregion

  //region setup
  private fun verifySetup() {
    loadThemedContext()

    val metaBundle = loadMetaDataFromManifest()

    readServicesResId(metaBundle)
    policyUrl = readPolicyUrl(metaBundle)
  }

  private fun loadThemedContext() {
    val tv = TypedValue()
    val theme = tv.loadAttr(theme, R.attr.gdpr_PolicyTheme).resourceId

    if (theme == 0) {
      throw IllegalStateException("Must specify gdpr_PolicyTheme in theme")
    }

    styledContext = ContextThemeWrapper(this, theme)
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

  private fun readPolicyUrl(metaBundle: Bundle): String {
    return intent.getStringExtra(EXTRA_URL)
        ?: metaBundle.getString(getString(R.string.gdpr_sdk__policy), null)
        ?: throw IllegalStateException(
          "No policy url set. Please specify " +
              "`<meta-data android:name=\"@string/gdpr_sdk__policy\" android:value=\"@string/privacy_policy\" />`" +
              " in your manifest, or use GDPRPolicyManager.policyUrl to set it at runtime."
        )
  }
  //endregion

  companion object {
    private const val TAG_SETTINGS = "settings"
    private const val EXTRA_URL = "url"
    private const val EXTRA_SCREEN_FLOW = "screenFlow"

    @JvmStatic
    @JvmOverloads
    fun newIntent(context: Context, policyUrl: String? = null): Intent =
      Intent(context, PolicyActivity::class.java).apply {
        putExtra(EXTRA_URL, policyUrl)
      }

    fun newOptInStandaloneIntent(context: Context, policyUrl: String? = null): Intent =
      newIntent(context, policyUrl)
          .putExtra(EXTRA_SCREEN_FLOW, ScreenFlow.OptInStandalone.ordinal)

    fun newOptInIntent(context: Context, policyUrl: String? = null): Intent =
      newIntent(context, policyUrl)
          .putExtra(EXTRA_SCREEN_FLOW, ScreenFlow.OptIn.ordinal)

    fun newOptOutIntent(context: Context, policyUrl: String? = null): Intent =
      newIntent(context, policyUrl)
          .putExtra(EXTRA_SCREEN_FLOW, ScreenFlow.OptOut.ordinal)
  }

}
