## Policy Manager Library

To support some GDPR requirements this library includes a WebView wrapper to show your terms and supplies an interface to update the timestamp when they were last accepted.

To comply with [Art. 21 GDPR - Right to object](https://gdpr-info.eu/art-21-gdpr/) links in the policy that end with the fragment `#app-opt-out` provide an option to opt-out from tracking done under [Art. 6 (e) or (f)](https://gdpr-info.eu/art-6-gdpr/).

This library provides the following GDPR-related features, which can be used independently of each other:
- Display a list of used services with opt-in and opt-out buttons
- Display a screen with legal terms, allowing the user to accept or decline

### List of Services

The list of services typically contains entries like Firebase Analytics, Crashlytics, etc. The app informs this library of the available services via meta-data in the app's manifest (see below). 

This screen is typically displayed in two scenarios:

 - Automatically, after a user has logged in for the first time (on a per-user basis)

   In this case, the user simply consents or declines, and there is no further screen flow.
 - Triggered explicitly by a user, when they wish to modify their current opt-in / opt-out setting

   Depending on whether the user opts in or out, there is a short screen flow that prompts the user to stay opted in, shows the current opt-in state, etc.

Note: The timestamps referred to below apply to the legal terms screen only and have nothing to do with this feature.

### Legal Terms Screen

The legal terms screen displays legal text obtained from a server.

The legal terms screen operates with two timestamps:

- **latest_policy** indicates when the legal text was last changed
- **policy_accepted** indicates when the user last accepted the terms

Together, the timestamps determine whether the user has accepted the current terms. The library uses this information to prompt the user to re-accept the terms after they have changed.

### Theming Support

The appearance of the screens shown by the library can be customized using Android's theme support. See `attrs.xml` in the library for details. For an example of how to 
override the styles, see `library/styles.xml` in this project, or look at the Gibble project. 

An slightly more advanced customization option is changing the order of the opt-in and opt-out buttons. This can be done via the `gdpr_positiveOnTop` attribute.

### Notes

When a service like Google Analytics is used by an app, it is usually initialized automatically when the app starts. To prevent a service starting before the user has given their 
consent, a meta-data item is usually available whose value can be set to `false`.

The name of the meta-data item is
- `firebase_analytics_collection_enabled` for Google Analytics
- `firebase_crashlytics_collection_enabled` for Crashlytics

## Setup

Start by adding some meta data to your `<application>` tag in the manifest. If your policy is not static you can also supply it later at runtime.

```xml
<meta-data
    android:name="@string/gdpr_sdk__policy"
    android:value="@string/privacy_policy" />

<meta-data android:name="@string/gdpr_sdk__services" android:resource="@xml/services"/>
```

Next create a `services.xml` in your `res/xml` directory.

```xml
<?xml version="1.0" encoding="utf-8"?>
<services>
    <service name="@string/service_analytics" />

    <service name="@string/service_firebase">
        <!-- You can optionally define some bindings if you provide your own layout -->
        <bind id="@android:id/text2">Description</bind>
    </service>
</services>
```

Finally, add the theme to your AppTheme that this library should use. It has reasonable defaults, but most properties can be overridden.

```xml
<!-- Base application theme. -->
<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
    <item name="colorAccent">@color/colorAccent</item>

    <!-- ... other code ... -->

    <item name="gdpr_PolicyTheme">@style/GDPR</item>
</style>
```

## Usage

### Registration

On your registration you should link to the terms of service that the user has to accept. You can use `PrivacyPolicySwitch` which provides a link next to a switch, or you can implement your own.
You can call `GDPRPolicyManager.instance().getPolicyIntent()` to get an intent to start the Activity.

```xml
<at.allaboutapps.gdpr.widget.PrivacyPolicySwitch
    android:id="@+id/policy_accepted"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="My text"
    android:paddingEnd="?android:listPreferredItemPaddingEnd"
    android:paddingStart="?android:listPreferredItemPaddingStart" />
```

### Login

After the login or acceptance it is _your responsibility_ to update the latest saved timestamp.

```kt
GDPRPolicyManager.instance().setPolicyAccepted(true) // if local, uses current timestamp
GDPRPolicyManager.instance().setPolicyAccepted(timestamp) // queried from server
```

### Policy changes

You should query your server about changes to the policy. Call `manager.updateLatestPolicyTimestamp(timestamp)` to update the cached data. You could do this at every app start, or from a background job.

#### Notify the user

In your Activity you can use `manager.shouldShowPolicy()` to check the current status and display a dialog.
You can extend `PolicyUpdateDialogFragment` if you need some further customizations on the dialog design.

```java
@Override
protected void onStart() {
    super.onStart();
    // ... other code

    if (GDPRPolicyManager.instance().shouldShowPolicy()) {
        PolicyUpdateDialogFragment.newInstance().show(getSupportFragmentManager());
    }
}
```

#### Send updated to the server

The library will send broadcasts when the user

1. accepts the policy, either from calling `setPolicyAccepted` or when the user accepts the new policy from the dialog
2. the user enabled or disabled additional services

`GdprServiceIntent.ACTION_POLICY_ACCEPTED` and `GdprServiceIntent.ACTION_SERVICES_CHANGED` will be sent respectively to any receivers registered in the app.

```xml
<receiver
        android:name=".ServicesBroadcastReceiver"
        android:exported="false">
    <intent-filter>
        <action android:name="at.allaboutapps.gdpr.SERVICES_CHANGED" />
        <action android:name="at.allaboutapps.gdpr.POLICY_ACCEPTED" />
    </intent-filter>
</receiver>
```

In this receiver you should send or queue the updates to the server.
