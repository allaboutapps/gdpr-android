package at.allaboutapps.gdprpolicysdk

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.CompoundButton
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

    //fetch latest policy version from server on a regular basis
    Handler().postDelayed({
      // update date of latest changes
      manager.updateLatestPolicyTimestamp(System.currentTimeMillis())
    }, 5 * 1000)

    policySwitch.checkedChangeListener = CompoundButton.OnCheckedChangeListener { _, checked ->
    }
  }

  override fun onStart() {
    super.onStart()

    // check if there is s change queued
    if (manager.shouldShowPolicy()) {
      // show dialog to accept / read
      PolicyUpdateDialogFragment.newInstance()
        .show(supportFragmentManager)
    }
  }
}