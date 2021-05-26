package at.allaboutapps.gdpr

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog

/**
 * Dialog prompt to inform the user of a changed policy. The user can read more or accept the updated terms.
 */
public open class PolicyUpdateDialogFragment : androidx.fragment.app.DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return AlertDialog.Builder(requireContext(), requireArguments().getInt(ARG_STYLE))
            .setCancelable(false)
            .setTitle(R.string.gdpr_sdk__popup_title)
            .setMessage(R.string.gdpr_sdk__popup_message)
            .setNeutralButton(R.string.gdpr_sdk__popup_viewPrivacyOption) { _, _ -> showPolicy() }
            .setPositiveButton(R.string.gdpr_sdk__popup_accept) { _, _ -> updateAcceptedTimestamp() }
            .apply { onPrepareDialogBuilder(this) }
            .create()
    }

    /**
     * Update / Modify the dialog to your needs. Title, message, neutral, and positive button are already set.
     */
    public open fun onPrepareDialogBuilder(builder: AlertDialog.Builder) = Unit

    private fun showPolicy() {
        startActivity(GDPRPolicyManager.instance().newSettingsIntent(true))
    }

    private fun updateAcceptedTimestamp() {
        val callback: PolicyAcceptedCallback? = when {
            parentFragment is PolicyAcceptedCallback -> parentFragment as PolicyAcceptedCallback
            activity is PolicyAcceptedCallback -> activity as PolicyAcceptedCallback
            else -> null
        }
        callback?.onPolicyAccepted()

        GDPRPolicyManager.instance().setPolicyAccepted(true)
    }

    /**
     * Show dialog if it is not already showing.
     */
    public fun show(fragmentManager: androidx.fragment.app.FragmentManager) {
        if (fragmentManager.findFragmentByTag(TAG) == null) {
            show(fragmentManager, TAG)
        }
    }

    public companion object {
        private const val TAG = "${BuildConfig.LIBRARY_PACKAGE_NAME}.GDPR_DIALOG"
        private const val ARG_STYLE = "${BuildConfig.LIBRARY_PACKAGE_NAME}.style"

        @JvmStatic
        @JvmOverloads
        public fun newInstance(@StyleRes style: Int = 0) =
            PolicyUpdateDialogFragment().addArguments {
                putInt(ARG_STYLE, style)
            }

        private inline fun <T : PolicyUpdateDialogFragment> T.addArguments(
            crossinline block: Bundle.() -> Unit
        ): T = this.apply { arguments = (arguments ?: Bundle()).also(block) }
    }

    public interface PolicyAcceptedCallback {
        public fun onPolicyAccepted()
    }
}
