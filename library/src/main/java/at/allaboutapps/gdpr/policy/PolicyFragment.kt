@file:Suppress("OverridingDeprecatedMember")

package at.allaboutapps.gdpr.policy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.webkit.WebView
import android.widget.ProgressBar
import at.allaboutapps.gdpr.BasePolicyFragment
import at.allaboutapps.gdpr.GDPRActivity
import at.allaboutapps.gdpr.R
import at.allaboutapps.gdpr.services.TextResource

class PolicyFragment : BasePolicyFragment(), PolicyWebViewClient.Callback {

    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var errorView: View

    private var showSettings = false
    private var showLoading = true
    private lateinit var loadingMethod: PolicyLoadingMethod

    override fun getTitle(): String =
        arguments?.getParcelable<TextResource>(ARG_TITLE)?.toString(requireContext()) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = arguments!!
        val uri = arguments.getString(ARG_URI)!!
        loadingMethod = UrlPolicyLoadingMethod(uri)

        showSettings = arguments.getBoolean(ARG_SHOW_SETTINGS)
        setHasOptionsMenu(showSettings)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.gdpr_sdk__menu_policy, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_action_settings -> {
                showSettings()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showSettings() {
        val intent = GDPRActivity.newIntent(requireContext(), showToSInfo = false)
        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.gdpr_sdk__fragment_policy, container, false)
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
            // todo can we make this optional?
            javaScriptEnabled = true // required to display complex policies on websites
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

    override fun onShowOptOutSelected(): Unit = showSettings()

    override fun onError() = showError()

    override fun onFinishedLoading() = showWebView()

    private fun showError() {
        webView.visibility = View.INVISIBLE
        progress.visibility = View.GONE

        if (errorView is ViewStub) {
            errorView = (errorView as ViewStub).inflate()
        }

        errorView.visibility = View.VISIBLE

        val view: View = errorView.findViewById(android.R.id.button1)
            ?: throw IllegalStateException("Must define `android.R.id.button1` for error retries in error layout")
        view.setOnClickListener { startLoadingContent() }
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_URI = "uri"
        private const val ARG_SHOW_SETTINGS = "show_settings"

        internal fun newInstance(
            title: TextResource,
            uri: String,
            showSettings: Boolean = true
        ): PolicyFragment = PolicyFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_TITLE, title)
                putString(ARG_URI, uri)
                putBoolean(ARG_SHOW_SETTINGS, showSettings)
            }
        }
    }
}
