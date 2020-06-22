package at.allaboutapps.gdpr.services

import android.content.Context
import android.os.Parcel
import android.os.Parcelable

internal data class TextResource(
    val stringResId: Int = 0,
    val rawText: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(stringResId)
        parcel.writeString(rawText)
    }

    fun toString(context: Context) =
        if (stringResId != 0) context.getString(stringResId) else rawText

    override fun describeContents(): Int = 0

    companion object CREATOR :
        Parcelable.Creator<TextResource> {
        override fun createFromParcel(parcel: Parcel): TextResource =
            TextResource(parcel)

        override fun newArray(size: Int): Array<TextResource?> = arrayOfNulls(size)
    }
}
