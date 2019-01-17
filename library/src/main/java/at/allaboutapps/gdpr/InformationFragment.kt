package at.allaboutapps.gdpr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.allaboutapps.gdpr.widget.ServicesView

class InformationFragment : BasePolicyFragment() {

  override fun themeOverrideAttr() = R.attr.gdpr_informationStyle
  override fun getTitle(): String =
    getString(R.string.gdpr_sdk__service_reactivated_navigation_title)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return styledInflater().inflate(R.layout.gdpr_policy_fragment_information, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val servicesResId = arguments!!.getInt(ARG_SERVICES)
    view.findViewById<ServicesView>(R.id.service_list).inflateServices(servicesResId)

    view.findViewById<View>(R.id.action_return)
      .setOnClickListener { policyActivity().onCloseClicked() }
  }

  companion object {
    private const val ARG_SERVICES = "services"

    fun newInstance(servicesResId: Int): InformationFragment = InformationFragment().apply {
      arguments = Bundle().apply {
        putInt(ARG_SERVICES, servicesResId)
      }
    }
  }

}
