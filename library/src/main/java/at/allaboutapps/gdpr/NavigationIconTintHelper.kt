package at.allaboutapps.gdpr

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

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
