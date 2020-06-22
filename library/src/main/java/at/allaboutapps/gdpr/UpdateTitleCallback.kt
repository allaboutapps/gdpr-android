package at.allaboutapps.gdpr

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

internal class UpdateTitleCallback(private val activity: GDPRActivity) :
    FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        if (f is BasePolicyFragment) activity.title = f.getTitle()
    }
}
