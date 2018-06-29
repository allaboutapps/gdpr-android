@file:Suppress("OverridingDeprecatedMember")

package at.allaboutapps.gdpr.policy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.webkit.WebView
import android.widget.ProgressBar
import at.allaboutapps.gdpr.BasePolicyFragment
import at.allaboutapps.gdpr.R

class PolicyFragment : BasePolicyFragment(), PolicyWebViewClient.Callback {

  private lateinit var webView: WebView
  private lateinit var progress: ProgressBar
  private lateinit var errorView: View

  private val showLoading = true
  private lateinit var loadingMethod: PolicyLoadingMethod

  override fun getTitle(): String = getString(R.string.gdpr_sdk__privacy_navigationtitle)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val uri = arguments!!.getString(ARG_URI)
    loadingMethod = UrlPolicyLoadingMethod(uri)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return styledInflater().inflate(R.layout.gdpr_policy_fragment_policy, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    webView = view.findViewById(R.id.webview)
    progress = view.findViewById(android.R.id.progress)
    errorView = view.findViewById(R.id.error)

    load()
  }

  override fun onBackPressed(): Boolean {
    if (webView.canGoBack()) {
      webView.goBack()
      return true
    }
    return false
  }

  private fun showProgress() {
    webView.visibility = View.INVISIBLE
    progress.visibility = View.VISIBLE
    errorView.visibility = View.GONE
  }

  private fun load() {
    webView.settings.run {
      javaScriptEnabled = false // don't for now
      builtInZoomControls = false
      setSupportZoom(false)
      displayZoomControls = false
    }

    webView.webViewClient = PolicyWebViewClient(this)

    startLoadingContent()
  }

  private fun showWebView() {
    webView.visibility = View.VISIBLE
    progress.visibility = View.GONE
    errorView.visibility = View.GONE
  }

  private fun startLoadingContent() {
    if (showLoading) {
      showProgress()
    } else {
      showWebView()
    }
    errorView.visibility = View.GONE
    loadingMethod.startLoading(webView)
  }

  override fun onShowOptOutSelected() = policyActivity().onShowOptOutSettingsSelected()

  override fun onError() = showError()

  override fun onFinishedLoading() = showWebView()

  private fun showError() {
    webView.visibility = View.INVISIBLE
    progress.visibility = View.GONE

    if (errorView is ViewStub) {
      errorView = (errorView as ViewStub).inflate()
    }

    errorView.visibility = View.VISIBLE

    val view: View = errorView.findViewById<View>(android.R.id.button1)
        ?: throw IllegalStateException("Must define `android.R.id.button1` for error retries in error layout")
    view.setOnClickListener { startLoadingContent() }
  }

  companion object {
    private const val ARG_URI = "uri"

    fun newInstance(uri: String): PolicyFragment {
      return PolicyFragment().apply {
        arguments = Bundle().apply {
          putString(ARG_URI, uri)
        }
      }
    }
  }
}
