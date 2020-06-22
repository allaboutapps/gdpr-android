package at.allaboutapps.gdpr.policy

import android.os.Parcelable
import android.webkit.WebView

/**
 * Handler interface for the content loading.
 *
 * @see DataPolicyLoadingMethod
 */
interface PolicyLoadingMethod : Parcelable {

    /**
     * Load the content into the `webView`
     *
     * @param webView the loading target
     */
    fun startLoading(webView: WebView)
}
