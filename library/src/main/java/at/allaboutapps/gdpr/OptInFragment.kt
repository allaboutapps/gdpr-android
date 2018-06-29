package at.allaboutapps.gdpr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.allaboutapps.gdpr.widget.ServicesView

class OptInFragment : BasePolicyFragment() {

  override fun getTitle(): String = getString(R.string.gdpr_sdk__service_disabled_navigation_title)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return styledInflater().inflate(R.layout.gdpr_policy_fragment_opt_in, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val servicesResId = arguments!!.getInt(ARG_SERVICES)
    view.findViewById<ServicesView>(R.id.service_list).inflateServices(servicesResId)

    view.findViewById<View>(R.id.action_enable_services)
      .setOnClickListener { policyActivity().onEnableServicesClicked() }
  }

  companion object {
    private const val ARG_SERVICES = "services"

    fun newInstance(servicesResId: Int): OptInFragment = OptInFragment().apply {
      arguments = Bundle().apply {
        putInt(ARG_SERVICES, servicesResId)
      }
    }
  }

}
