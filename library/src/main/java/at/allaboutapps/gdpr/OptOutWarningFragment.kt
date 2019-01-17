package at.allaboutapps.gdpr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.allaboutapps.gdpr.widget.ServicesView

class OptOutWarningFragment : BasePolicyFragment() {


  override fun themeOverrideAttr() = R.attr.gdpr_warningStyle
  override fun getTitle(): String = getString(R.string.gdpr_sdk__warning)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = styledInflater().inflate(R.layout.gdpr_policy_fragment_warning_opt_out, container, false)

    applyButtonOrder(view, R.id.action_opt_out, R.id.action_return)

    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val servicesResId = arguments!!.getInt(ARG_SERVICES)
    view.findViewById<ServicesView>(R.id.service_list).inflateServices(servicesResId)

    view.findViewById<View>(R.id.action_return)
      .setOnClickListener { policyActivity().onCloseClicked() }
    view.findViewById<View>(R.id.action_opt_out)
      .setOnClickListener { policyActivity().onDisableServicesClicked() }
  }

  companion object {
    private const val ARG_SERVICES = "services"

    fun newInstance(servicesResId: Int): OptOutWarningFragment = OptOutWarningFragment().apply {
      arguments = Bundle().apply {
        putInt(ARG_SERVICES, servicesResId)
      }
    }
  }

}
