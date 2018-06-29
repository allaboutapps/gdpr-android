package at.allaboutapps.gdpr

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager

internal class UpdateTitleCallback(private val activity: PolicyActivity) :
  FragmentManager.FragmentLifecycleCallbacks() {

  override fun onFragmentStarted(fm: FragmentManager?, f: Fragment) {
    if (f is BasePolicyFragment) activity.title = f.getTitle()
  }
}