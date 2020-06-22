package at.allaboutapps.gdpr

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class BasePolicyFragment : androidx.fragment.app.Fragment() {

    open fun themeOverrideAttr(): Int = 0

    fun policyActivity() = requireActivity() as PolicyActivity

    val styledContext = lazy {
        val baseContext = (requireActivity() as PolicyActivity).styledContext
        if (themeOverrideAttr() == 0)
            baseContext
        else {
            val tv = TypedValue()
            val theme = tv.loadAttr(baseContext.theme, themeOverrideAttr()).resourceId
            ContextThemeWrapper(baseContext, theme)
        }
    }
    fun styledInflater() = LayoutInflater.from(styledContext.value)

    open fun onBackPressed(): Boolean = false

    abstract fun getTitle(): String

    fun applyButtonOrder(view: View, declineButtonId: Int, consentButtonId: Int) {
        val typedValue = TypedValue()
        val consentButtonAtTop = typedValue.loadAttr(styledContext.value.theme, R.attr.gdpr_positiveOnTop).data != 0

        if (consentButtonAtTop) {
            val declineButton = view.findViewById<View>(declineButtonId)
            val consentButton = view.findViewById<View>(consentButtonId)
            val buttonParent = consentButton.parent as ViewGroup
            val declineButtonIndex = buttonParent.indexOfChild(declineButton)

            buttonParent.removeView(consentButton)
            buttonParent.addView(consentButton, declineButtonIndex)
        }
    }
}
