package at.allaboutapps.gdprpolicysdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.allaboutapps.gdpr.GDPRPolicyManager
import at.allaboutapps.gdpr.PolicyUpdateDialogFragment
import at.allaboutapps.gdpr.widget.PrivacyPolicySwitch

class MainActivity : AppCompatActivity() {

    private lateinit var manager: GDPRPolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val policySwitch = findViewById<PrivacyPolicySwitch>(R.id.policy_accepted)

        manager = GDPRPolicyManager.instance()

        // fetch latest policy version from server on a regular basis
        Handler().postDelayed(
            {
                // update date of latest changes
                manager.updateLatestPolicyTimestamp(System.currentTimeMillis())
            },
            5 * 1000
        )

        policySwitch.checkedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ ->
        }

        findViewById<View>(R.id.confirm_tos).setOnClickListener {
            startResult(manager.newConfirmationIntent(requireToS = true, showSettings = false))
        }
        findViewById<View>(R.id.tos_settings).setOnClickListener {
            startResult(manager.newConfirmationIntent(requireToS = true, showSettings = true))
        }
        findViewById<View>(R.id.settings).setOnClickListener {
            startResult(manager.newConfirmationIntent(requireToS = false, showSettings = true))
        }
        findViewById<View>(R.id.tracking).setOnClickListener {
            startActivity(manager.newSettingsIntent())
        }
        findViewById<View>(R.id.tracking_w_terms_notics).setOnClickListener {
            startActivity(manager.newSettingsIntent(showToSInfo = true))
        }
        findViewById<View>(R.id.policy).setOnClickListener {
            startActivity(manager.getPolicyIntent())
        }
        findViewById<View>(R.id.tos).setOnClickListener {
            startActivity(manager.getTOSIntent())
        }
        findViewById<View>(R.id.dialog).setOnClickListener {
            // show dialog to accept / read
            PolicyUpdateDialogFragment.newInstance().show(supportFragmentManager)
        }
    }

    private fun startResult(intent: Intent) {
        startActivityForResult(intent, RC_CONFIRM_POLICY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_CONFIRM_POLICY -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Confirmed", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    companion object {
        private const val RC_CONFIRM_POLICY = 33
    }
}
