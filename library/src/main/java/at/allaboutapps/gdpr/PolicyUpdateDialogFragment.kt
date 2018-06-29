package at.allaboutapps.gdpr

import android.app.Dialog
import android.os.Bundle
import android.support.annotation.StyleRes
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog

/**
 * Dialog prompt to inform the user of a changed policy. The user can read more or accept the updated terms.
 */
open class PolicyUpdateDialogFragment : DialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    isCancelable = false
    return AlertDialog.Builder(requireContext(), arguments!!.getInt(ARG_STYLE))
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
  open fun onPrepareDialogBuilder(builder: AlertDialog.Builder) = Unit

  private fun showPolicy() {
    startActivity(GDPRPolicyManager.instance().getPolicyIntent())
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
  fun show(fragmentManager: FragmentManager) {
    if (fragmentManager.findFragmentByTag(TAG) == null) {
      show(fragmentManager, TAG)
    }
  }

  companion object {
    private const val TAG = "${BuildConfig.APPLICATION_ID}.GDPR_DIALOG"
    private const val ARG_STYLE = "${BuildConfig.APPLICATION_ID}.style"

    fun newInstance(@StyleRes style: Int = 0) = PolicyUpdateDialogFragment().apply {
      arguments = Bundle().apply {
        putInt(ARG_STYLE, style)
      }
    }
  }

  interface PolicyAcceptedCallback {
    fun onPolicyAccepted()
  }

}