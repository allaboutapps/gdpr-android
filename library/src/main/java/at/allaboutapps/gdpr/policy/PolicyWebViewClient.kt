package at.allaboutapps.gdpr.policy

import android.content.Intent
import android.net.MailTo
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient

class PolicyWebViewClient(private val callback: Callback) : WebViewClient() {

    private var failedUrl: String? = null

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

        if (Uri.parse(url).encodedFragment == FRAGMENT_OPT_OUT) {
            callback.onShowOptOutSelected()
            return true
        }

        val intent = when {
            isHttpUrl(url) -> newUrlIntent(url)
            isPhoneNumberUrl(url) -> Intent(Intent.ACTION_DIAL, Uri.parse(url))
            isMailToUrl(url) -> {
                val mt = MailTo.parse(url)
                newEmailIntent(mt.to, mt.subject, mt.body, mt.cc)
            }
            else -> return false
        }
        view.context.startActivity(Intent.createChooser(intent, ""))
        return true
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        if (Uri.parse(url).encodedFragment == FRAGMENT_OPT_OUT) {
            callback.onShowOptOutSelected()
        }
    }

    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        failedUrl = failingUrl
        callback.onError()
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (failedUrl != null && failedUrl == url) {
            failedUrl = null
            return
        }
        callback.onFinishedLoading()
    }

    private fun isPhoneNumberUrl(url: String): Boolean = url.startsWith(PREFIX_TEL, ignoreCase = true)
    private fun isMailToUrl(url: String): Boolean = url.startsWith(PREFIX_MAIL_TO, ignoreCase = true)
    private fun isHttpUrl(url: String): Boolean =
        url.startsWith(PREFIX_HTTPS, ignoreCase = true) ||
            url.startsWith(PREFIX_HTTP, ignoreCase = true)

    private fun newUrlIntent(url: String): Intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))

    private fun newEmailIntent(
        address: String,
        subject: String?,
        body: String?,
        cc: String?
    ): Intent =
        Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null))
            .putExtra(Intent.EXTRA_EMAIL, address)
            .putExtra(Intent.EXTRA_TEXT, body)
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_CC, cc)

    interface Callback {

        fun onShowOptOutSelected()

        fun onError()

        fun onFinishedLoading()
    }

    companion object {
        private const val PREFIX_HTTP = "http:"
        private const val PREFIX_HTTPS = "https:"
        private const val PREFIX_TEL = "tel:"
        private const val PREFIX_MAIL_TO = "mailto:"
        /**
         * Links to refer to opt out must be of the form `#fragment`.
         */
        private const val FRAGMENT_OPT_OUT = "app-opt-out"
    }
}
