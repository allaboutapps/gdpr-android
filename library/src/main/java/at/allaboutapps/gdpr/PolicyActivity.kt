package at.allaboutapps.gdpr

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.graphics.ColorUtils
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.MenuItem
import at.allaboutapps.gdpr.policy.PolicyFragment

class PolicyActivity : AppCompatActivity() {

  internal lateinit var styledContext: Context
  private var servicesResId: Int = 0

  private var colorPrimary = 0
  private var colorPrimaryDark = 0
  private var colorAccent = 0
  private var colorWarning = 0

  private lateinit var policyUrl: String

  private lateinit var tintHelper: NavigationIconTintHelper

  var gdprManager: GDPRPolicyManager = GDPRPolicyManager.instance()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    verifySetup()

    val typedValue = TypedValue()
    val styledTheme = styledContext.theme
    colorPrimary = typedValue.loadAttr(styledTheme, R.attr.colorPrimary).data
    colorPrimaryDark = typedValue.loadAttr(styledTheme, R.attr.colorPrimaryDark).data
    colorAccent = typedValue.loadAttr(styledTheme, R.attr.colorAccent).data
    colorWarning = typedValue.loadAttr(styledTheme, R.attr.gdpr_colorWarning).data

    supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    tintHelper = NavigationIconTintHelper(supportActionBar!!, styledContext)

    supportFragmentManager.addOnBackStackChangedListener { updateNavigationIcon() }
    supportFragmentManager.registerFragmentLifecycleCallbacks(UpdateTitleCallback(this), false)

    updateNavigationIcon()

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(android.R.id.content, PolicyFragment.newInstance(policyUrl))
        .commit()
    }
  }

  fun onShowOptOutSettingsSelected() {
    val fragment = if (areServicesEnabled()) {
      updateToolbarColor(colorWarning)
      OptOutWarningFragment.newInstance(servicesResId)
    } else {
      updateToolbarColor(colorAccent)
      OptInFragment.newInstance(servicesResId)
    }

    addSettingsFragment(fragment)
  }

  private fun addSettingsFragment(fragment: BasePolicyFragment) {
    supportFragmentManager.popBackStack(TAG_SETTINGS, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    supportFragmentManager.beginTransaction()
      .replace(android.R.id.content, fragment)
      .addToBackStack(TAG_SETTINGS)
      .commit()
  }

  private fun updateToolbarColor(color: Int) {
    val statusBarColor = ColorUtils.blendARGB(color, Color.BLACK, 0.15F)
    setToolbarColors(color, statusBarColor)
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

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  private fun updateNavigationIcon() {
    val canGoBack = supportFragmentManager.backStackEntryCount == 0

    if (canGoBack) {
      setToolbarColors(colorPrimary, colorPrimaryDark)
    }

    tintHelper.updateNavigationIcon(canGoBack)
  }

  // region callbacks
  fun onDisableServicesClicked() {
    gdprManager.servicesEnabled = false

    onShowOptOutSettingsSelected()
  }

  fun onCloseClicked() {
    finish()
  }

  fun onEnableServicesClicked() {
    gdprManager.servicesEnabled = true

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

    @JvmStatic
    @JvmOverloads
    fun newIntent(context: Context, policyUrl: String? = null): Intent =
      Intent(context, PolicyActivity::class.java).apply {
        putExtra(EXTRA_URL, policyUrl)
      }
  }

}
