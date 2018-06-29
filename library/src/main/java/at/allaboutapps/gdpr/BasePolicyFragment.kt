package at.allaboutapps.gdpr

import android.support.v4.app.Fragment
import android.view.LayoutInflater

abstract class BasePolicyFragment : Fragment() {

  fun policyActivity() = requireActivity() as PolicyActivity

  fun styledContext() = (requireActivity() as PolicyActivity).styledContext
  fun styledInflater() = LayoutInflater.from(styledContext())

  open fun onBackPressed(): Boolean = false

  abstract fun getTitle(): String
}
