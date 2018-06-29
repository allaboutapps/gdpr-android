## Policy Manager Library

To support some GDPR requirements this library includes a WebView wrapper to show your terms and supplies an interface to update the timestamp when they were last accepted.

To comply with [Art. 21 GDPR - Right to object](https://gdpr-info.eu/art-21-gdpr/) links in the policy that end with the fragment `#app-opt-out` provide an option to opt-out from tracking done under [Art. 6 (e) or (f)](https://gdpr-info.eu/art-6-gdpr/).

## Setup

Start by adding some meta data to your `<application>` tag in the manifest. If your policy is not static you can also supply it later at runtime.

    <meta-data
        android:name="@string/gdpr_sdk__policy"
        android:value="@string/privacy_policy" />

    <meta-data android:name="@string/gdpr_sdk__services" android:resource="@xml/services"/>

Next create a `services.xml` in your `values/xml` directory.

    <?xml version="1.0" encoding="utf-8"?>
    <services>
        <service name="@string/service_analytics" />

        <service name="@string/service_firebase">
            <!-- You can optionally define some bindings if you provide your own layout -->
            <bind id="@android:id/text2">Description</bind>
        </service>
    </services>

## Usage

### Registration

On your registration you should link to the terms of service that the user has to accept. You can use `PrivacyPolicySwitch` which provides a link next to a switch, or you can implement your own.
You can call `GDPRPolicyManager.instance().getPolicyIntent()` to get an intent to start the Activity.

    <at.allaboutapps.gdpr.widget.PrivacyPolicySwitch
        android:id="@+id/policy_accepted"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="My text"
        android:paddingEnd="?android:listPreferredItemPaddingEnd"
        android:paddingStart="?android:listPreferredItemPaddingStart" />

### Login

After the login or acceptance it is _your responsibility_ to update the latest saved timestamp.

    GDPRPolicyManager.instance().setPolicyAccepted(true) // if local, uses current timestamp
    GDPRPolicyManager.instance().setPolicyAccepted(timestamp) // queried from server

### Policy Changes

You should query your server about changes to the policy. Call `manager.updateLatestPolicyTimestamp(timestamp)` to update the cached data. You could do this at every app start, or from a background job.

In your Activity you can use `manager.shouldShowPolicy()` to check the current status and display a dialog.
You can extend `PolicyUpdateDialogFragment` if you need some further customizations on the dialog design.

    if (manager.shouldShowPolicy()) {
      // show dialog to accept / read
      PolicyUpdateDialogFragment.newInstance()
        .show(supportFragmentManager)
    }