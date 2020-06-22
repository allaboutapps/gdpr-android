package at.allaboutapps.gdpr.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.allaboutapps.gdpr.ListItem
import at.allaboutapps.gdpr.R
import at.allaboutapps.gdpr.SettingsViewModel
import at.allaboutapps.gdpr.services.TextResource

internal class ServiceAdapter(
    private val viewModel: SettingsViewModel,
    private val showTos: () -> Unit,
    private val showPrivacyPolicy: (showSettings: Boolean) -> Unit
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(Callback()) {

    class Callback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
            (oldItem is ListItem.Service && newItem is ListItem.Service && oldItem.id == newItem.id) || oldItem == newItem

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
            oldItem == newItem
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val buttonPrivacyPolicy: View = view.findViewById(R.id.action_privacy)
    }

    class PrivacyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val buttonPrivacyPolicy: View = view.findViewById(R.id.action_privacy)
    }

    class TOSViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.checkbox_tos_accepted)
        val buttonTos: View = view.findViewById(R.id.action_tos)
    }

    class TOSInfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val buttonTos: View = view.findViewById(R.id.action_tos)
    }

    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val switch: SwitchCompat = view.findViewById(R.id.title_switch)
        val description: TextView = view.findViewById(R.id.description)
        val clearButton: Button = view.findViewById(R.id.action_delete)
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ListItem.TermsOfService -> TOS
        is ListItem.TermsOfServiceInfo -> TOS_INFO
        is ListItem.ServicesHeader -> HEADER
        is ListItem.PrivacyInfo -> PRIVACY
        else -> SERVICE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            HEADER -> {
                val view =
                    inflater.inflate(R.layout.gdpr_sdk__item_services_header, parent, false)
                HeaderViewHolder(view)
            }
            TOS -> {
                val view =
                    inflater.inflate(R.layout.gdpr_sdk__item_terms_of_service, parent, false)
                TOSViewHolder(view)
            }
            TOS_INFO -> {
                val view =
                    inflater.inflate(R.layout.gdpr_sdk__item_terms_of_service_info, parent, false)
                TOSInfoViewHolder(view)
            }
            PRIVACY -> {
                val view =
                    inflater.inflate(R.layout.gdpr_sdk__policy_item_privacy_notice, parent, false)
                PrivacyViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.gdpr_sdk__item_service, parent, false)
                ServiceViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ServiceViewHolder -> {
                val service = getItem(position) as ListItem.Service
                holder.switch.setText(service.name)
                holder.switch.setOnCheckedChangeListener(null)
                holder.switch.isChecked = service.enabled
                holder.switch.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.setEnabled(service.id, isChecked)
                }

                holder.description.setText(service.description)
                holder.description.visibility =
                    if (holder.description.text.isEmpty()) View.GONE else View.VISIBLE

                holder.clearButton.visibility = if (service.canClear) View.VISIBLE else View.GONE
                holder.clearButton.setOnClickListener { viewModel.clearService(service.id) }
            }
            is PrivacyViewHolder -> {
                holder.buttonPrivacyPolicy.setOnClickListener { showPrivacyPolicy(true) }
            }
            is HeaderViewHolder -> {
                holder.buttonPrivacyPolicy.setOnClickListener { showPrivacyPolicy(false) }
                holder.itemView.findViewById<View>(R.id.action_allow_all).setOnClickListener {
                    viewModel.enableAll()
                }
            }
            is TOSViewHolder -> {
                holder.buttonTos.setOnClickListener { showTos() }
                holder.checkbox.setOnCheckedChangeListener(null)
                holder.checkbox.isChecked = viewModel.tosAccepted.value ?: false
                holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.setTOSAccepted(isChecked)
                }
            }
            is TOSInfoViewHolder -> {
                holder.buttonTos.setOnClickListener { showTos() }
            }
        }
    }

    companion object {
        const val SERVICE = 1
        const val HEADER = 2
        const val TOS = 3
        const val PRIVACY = 4
        const val TOS_INFO = 5
    }

    private fun TextView.setText(text: TextResource) {
        if (text.stringResId == 0) {
            setText(text.rawText)
        } else {
            setText(text.stringResId)
        }
    }
}
