package at.allaboutapps.gdpr

import android.content.Context
import android.content.res.Resources
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.ActionBar
import android.util.TypedValue
import android.view.ContextThemeWrapper

internal class NavigationIconTintHelper(private val supportActionBar: ActionBar, context: Context) {

  private val resources = context.resources
  private val tintColor: Int
  private val toolbarTheme: Resources.Theme

  init {
    val typedValue = TypedValue()
    val toolbarStyle = typedValue.loadAttr(context.theme, R.attr.actionBarTheme).resourceId
    val toolbarStyledContext = ContextThemeWrapper(context, toolbarStyle)
    toolbarTheme = toolbarStyledContext.theme

    typedValue.loadAttr(toolbarTheme, R.attr.colorControlNormal)
    tintColor = if (typedValue.resourceId != 0) {
      ContextCompat.getColor(toolbarStyledContext, typedValue.resourceId)
    } else {
      typedValue.data
    }
  }

  // supportFragmentManager.backStackEntryCount == 0
  fun updateNavigationIcon(canGoBack: Boolean) {
    val iconRes = if (canGoBack) {
      R.drawable.ic_close_black_24dp
    } else {
      R.drawable.ic_arrow_back_black_24dp
    }

    val icon = VectorDrawableCompat.create(resources, iconRes, toolbarTheme)!!
    DrawableCompat.setTint(icon, tintColor)
    supportActionBar.setHomeAsUpIndicator(icon)
  }
}