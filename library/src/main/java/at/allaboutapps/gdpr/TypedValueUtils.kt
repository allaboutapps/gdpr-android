package at.allaboutapps.gdpr

import android.content.res.Resources
import android.support.annotation.AttrRes
import android.util.TypedValue

internal fun TypedValue.loadAttr(theme: Resources.Theme, @AttrRes resId: Int): TypedValue {
  theme.resolveAttribute(resId, this, true)
  return this
}