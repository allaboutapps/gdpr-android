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
) : ListAdapter<ListItem, ServiceAdapter.Holder>(Callback()) {

    class Callback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
            (oldItem is ListItem.Service && newItem is ListItem.Service && oldItem.id == newItem.id) || oldItem == newItem

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
            oldItem == newItem
    }

    internal abstract class Holder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: ListItem)
    }

    private inner class HeaderViewHolder(view: View) : Holder(view) {
        private val buttonPrivacyPolicy: View = view.findViewById(R.id.action_privacy)

        override fun bind(item: ListItem) {
            buttonPrivacyPolicy.setOnClickListener { showPrivacyPolicy(false) }
            itemView.findViewById<View>(R.id.action_allow_all).setOnClickListener {
                viewModel.enableAll()
            }
        }
    }

    private inner class PrivacyViewHolder(view: View) : Holder(view) {
        private val buttonPrivacyPolicy: View = view.findViewById(R.id.action_privacy)

        override fun bind(item: ListItem) {
            buttonPrivacyPolicy.setOnClickListener { showPrivacyPolicy(true) }
        }
    }

    private inner class TOSViewHolder(view: View) : Holder(view) {
        private val checkbox: CheckBox = view.findViewById(R.id.checkbox_tos_accepted)
        private val buttonTos: View = view.findViewById(R.id.action_tos)

        override fun bind(item: ListItem) {
            buttonTos.setOnClickListener { showTos() }
            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = viewModel.tosAccepted.value ?: false
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setTOSAccepted(isChecked)
            }
        }
    }

    private inner class TOSInfoViewHolder(view: View) : Holder(view) {
        private val buttonTos: View = view.findViewById(R.id.action_tos)

        override fun bind(item: ListItem) {
            buttonTos.setOnClickListener { showTos() }
        }
    }

    private inner class ServiceViewHolder(view: View) : Holder(view) {
        private val switch: SwitchCompat = view.findViewById(R.id.title_switch)
        private val description: TextView = view.findViewById(R.id.description)
        private val clearButton: Button = view.findViewById(R.id.action_delete)

        override fun bind(item: ListItem) {
            val service = item as ListItem.Service
            switch.setText(service.name)
            switch.setOnCheckedChangeListener(null)
            switch.isChecked = service.enabled
            switch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setEnabled(service.id, isChecked)
            }

            description.setText(service.description)
            description.visibility =
                if (description.text.isEmpty()) View.GONE else View.VISIBLE

            clearButton.visibility = if (service.canClear) View.VISIBLE else View.GONE
            clearButton.setOnClickListener { viewModel.clearService(service.id) }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ListItem.TermsOfService -> TOS
        is ListItem.TermsOfServiceInfo -> TOS_INFO
        is ListItem.ServicesHeader -> HEADER
        is ListItem.PrivacyInfo -> PRIVACY
        else -> SERVICE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        val (layoutResId, holder) = when (viewType) {
            HEADER ->
                R.layout.gdpr_sdk__item_services_header to ::HeaderViewHolder
            TOS ->
                R.layout.gdpr_sdk__item_terms_of_service to ::TOSViewHolder
            TOS_INFO ->
                R.layout.gdpr_sdk__item_terms_of_service_info to ::TOSInfoViewHolder
            PRIVACY ->
                R.layout.gdpr_sdk__policy_item_privacy_notice to ::PrivacyViewHolder
            else ->
                R.layout.gdpr_sdk__item_service to ::ServiceViewHolder
        }

        val view = inflater.inflate(layoutResId, parent, false)
        return holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bind(getItem(position))

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
