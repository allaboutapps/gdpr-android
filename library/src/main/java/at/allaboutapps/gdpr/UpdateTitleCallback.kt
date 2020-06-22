package at.allaboutapps.gdpr

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

internal class UpdateTitleCallback(private val activity: PolicyActivity) :
    androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentStarted(fm: androidx.fragment.app.FragmentManager, f: androidx.fragment.app.Fragment) {
        if (f is BasePolicyFragment) activity.title = f.getTitle()
    }
}
