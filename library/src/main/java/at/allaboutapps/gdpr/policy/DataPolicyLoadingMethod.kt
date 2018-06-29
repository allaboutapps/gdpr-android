package at.allaboutapps.gdpr.policy

import android.os.Parcel
import android.os.Parcelable
import android.webkit.WebView

/** Loads a string into the `WebView`. e.g. if the server sent HTML to display within json.  */
internal class DataPolicyLoadingMethod : PolicyLoadingMethod, Parcelable {

  private val mData: String
  private val mMimeType: String
  private val mEncoding: String
  private val mBaseUrl: String?

  @JvmOverloads constructor(
    data: String, mimeType: String, encoding: String, baseUrl: String? = null
  ) {
    mBaseUrl = baseUrl
    mData = data
    mMimeType = mimeType
    mEncoding = encoding
  }

  protected constructor(parcel: Parcel) {
    this.mBaseUrl = parcel.readString()
    this.mData = parcel.readString()
    this.mMimeType = parcel.readString()
    this.mEncoding = parcel.readString()
  }

  override fun startLoading(webView: WebView) {
    if (mBaseUrl == null) {
      webView.loadData(mData, mMimeType, mEncoding)
    } else {
      webView.loadDataWithBaseURL(mBaseUrl, mData, mMimeType, mEncoding, null)
    }
  }

  override fun describeContents(): Int {
    return 0
  }

  override fun writeToParcel(dest: Parcel, flags: Int) {
    dest.writeString(this.mBaseUrl)
    dest.writeString(this.mData)
    dest.writeString(this.mMimeType)
    dest.writeString(this.mEncoding)
  }

  companion object CREATOR : Parcelable.Creator<DataPolicyLoadingMethod> {
    override fun createFromParcel(parcel: Parcel): DataPolicyLoadingMethod {
      return DataPolicyLoadingMethod(parcel)
    }

    override fun newArray(size: Int): Array<DataPolicyLoadingMethod?> {
      return arrayOfNulls(size)
    }
  }

}
