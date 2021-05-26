package at.allaboutapps.gdpr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.allaboutapps.gdpr.services.TextResource
import at.allaboutapps.gdpr.widget.ServiceAdapter

public class SettingsFragment : BasePolicyFragment() {

    private lateinit var adapter: ServiceAdapter
    private lateinit var viewModel: SettingsViewModel

    override fun getTitle(): String = when (viewModel.tosMode) {
        SettingsViewModel.TOS_CONFIRM -> getString(R.string.gdpr_sdk__settings_confirmation_title)
        else -> getString(R.string.gdpr_sdk__settings_title)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val servicesResId = requireArguments().getInt(ARG_SERVICES)
        val factory = ViewModelFactory(servicesResId, requireContext())
        viewModel = ViewModelProvider(requireActivity(), factory).get(SettingsViewModel::class.java)

        val args = requireArguments()
        viewModel.tosMode = args.getInt(ARG_REQUIRE_TOS, SettingsViewModel.TOS_HIDE)
        viewModel.showSettings = args.getBoolean(ARG_SHOW_SETTINGS, true)

        viewModel.loadServiceItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.gdpr_sdk__fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list: RecyclerView = view.findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(context)
        list.addItemDecoration(DividerItemDecoration(context))

        adapter = ServiceAdapter(viewModel, this::showTos, this::showPrivacyPolicy)
        list.adapter = adapter

        viewModel.listItems.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    private fun showTos() {
        val intent = GDPRActivity.newTOSIntent(requireContext())
        startActivity(intent)
    }

    private fun showPrivacyPolicy(showSettings: Boolean = false) {
        val intent = GDPRActivity.newPolicyIntent(requireContext(), showSettings)
        startActivity(intent)
    }

    internal companion object {
        private const val ARG_SERVICES = "services"
        private const val ARG_REQUIRE_TOS = "require_tos"
        private const val ARG_SHOW_SETTINGS = "show_settings"

        internal fun newInstance(
            servicesResId: Int,
            tosMode: Int,
            showSettings: Boolean
        ): SettingsFragment = SettingsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_SERVICES, servicesResId)
                putInt(ARG_REQUIRE_TOS, tosMode)
                putBoolean(ARG_SHOW_SETTINGS, showSettings)
            }
        }
    }
}

internal sealed class ListItem {
    object TermsOfService : ListItem()
    object TermsOfServiceInfo : ListItem()
    object PrivacyInfo : ListItem()
    object ServicesHeader : ListItem()
    data class Service(
        val id: Int,
        val name: TextResource,
        val description: TextResource,
        val enabled: Boolean,
        val canClear: Boolean
    ) : ListItem()
}
