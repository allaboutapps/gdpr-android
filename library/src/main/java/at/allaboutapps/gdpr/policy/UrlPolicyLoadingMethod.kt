package at.allaboutapps.gdpr.policy

import android.os.Parcel
import android.os.Parcelable
import android.webkit.WebView

/**
 * Loads an url into the WebView.
 */
class UrlPolicyLoadingMethod : PolicyLoadingMethod, Parcelable {

    private var mUrl: String? = null

    constructor(url: String) {
        mUrl = url
    }

    protected constructor(parcel: Parcel) {
        this.mUrl = parcel.readString()
    }

    override fun startLoading(webView: WebView) {
        webView.loadUrl(mUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.mUrl)
    }

    companion object CREATOR : Parcelable.Creator<UrlPolicyLoadingMethod> {
        override fun createFromParcel(parcel: Parcel): UrlPolicyLoadingMethod {
            return UrlPolicyLoadingMethod(parcel)
        }

        override fun newArray(size: Int): Array<UrlPolicyLoadingMethod?> {
            return arrayOfNulls(size)
        }
    }
}
