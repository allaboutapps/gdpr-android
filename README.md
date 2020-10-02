# Policy Manager Library

To support GDPR requirements this library helps manage settings for different tracking services and supports multiple flows to manage when a user agrees to the terms of service and/or the privacy policy.

Those features include:

* Display a list of integrated tracking services that can be enabled or disabled. If supported, they a user can also have their data deleted.
* Display the terms of service / privacy policy in a separate Activity / WebView

## Tracking Settings

The list of integrated services typically contains entries like Firebase Analytics, Crashlytics, etc. The list can be customized via xml and an attribute in the app's manifest.

Each service can show its title and description. They can be enabled or disabled individually and (optionally) provide an option to have the user data deleted.

## Setup

Be sure to run the demo `app` to get an idea on what this library can do for you!

Start by adding the URLs for your terms of service and privacy policy as `<meta-data />` to your `<application>` tag in the manifest. If your policies don't have a static URL then you can always set them later at runtime. Also include the ID of your XML file that holds the list of integrated services.

```xml
<meta-data
    android:name="@string/gdpr_sdk__tos"
    android:value="@string/tos" />
<meta-data
    android:name="@string/gdpr_sdk__policy"
    android:value="@string/privacy_policy" />
<meta-data
    android:name="@string/gdpr_sdk__services"
    android:resource="@xml/gdpr_services" />
```

Next, create a `services.xml` resource file in your `res/xml` directory. Here you can list the services integrated in your app.

**`<services />`:**

* `isOptIn <true|false>`: Defines the default setting for your services, whether they should be optIn by default. If set to `true`, all services will be disabled by default.

**`<service />`:**

* `id <String>` The ID of the service. This will be used to distinguish between which services are enabled.
* `name <String|StringResource>` The name of the service
* `description <String|StringResource>` The description of the service (optional)
* `isOptIn <true|false>` Whether this service should be optIn and disabled by default. Defaults to `services.isOptIn`.
* `supportsDeletion <true|false>` Whether this service supports deletion of user data. Defaults to `false`

For an example see [`app/../gdpr_services.xml`](/app/src/main/res/xml/gdpr_services.xml)

### Style & Theme

The appearance (colors, fonts, ...) can be customized using Android's themes. A few custom attributes were introduced for the styling of the list elements and need to be set as a theme for the `GDPRActivity`.

```xml
<!-- You can simply extend your app theme and add those 4 attributes,
       or use any of the AppCompat themes as parent -->
<style name="AppTheme.GDPR" parent="Theme.AppCompat.DayNight.DarkActionBar">
    <!-- Text for subheaders in the settings -->
    <item name="gdpr_headerTextAppearance">@style/TextAppearance.AppCompat.Body2</item>
    <!-- Body text for descriptions and such -->
    <item name="gdpr_textAppearance">@style/TextAppearance.AppCompat.Body1</item>
    <!-- Style for the button at the bottom of the confirmation flow -->
    <item name="gdpr_confirmButtonStyle">@style/Widget.AppCompat.Button.Borderless.Colored</item>
    <!-- Style for the "links" to the ToS and Privacy Policy -->
    <item name="gdpr_linkButtonStyle">@style/LinkButton</item>

    <!-- Style for other buttons (accept all, delete) -->
    <item name="borderlessButtonStyle">@style/BorderlessButton</item>
    <!-- Switch style -->
    <item name="switchStyle">@style/Switch</item>
</style>
```

Make sure to use an AppCompat or Bridge (Material Components) theme for this Activity.

```xml
<activity
    android:name="at.allaboutapps.gdpr.GDPRActivity"
    android:theme="@style/AppTheme.GDPR" />
```

## Usage

### Registration

On your registration you should link to the terms of service and privacy policy that the user has to accept. You can use `PrivacyPolicySwitch` which provides a link next to a switch, or you can implement your own.  
You can use `GDPRPolicyManager.instance().newSettingsIntent(showToSInfo = true)` to get an intent to start the Settings Activity.

```xml
<at.allaboutapps.gdpr.widget.PrivacyPolicySwitch
    android:id="@+id/policy_accepted"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="I accept the <annotation link="privacy">Privacy Policy</annotation> and <annotation link="tos">Terms of Service</annotation>"
    android:paddingEnd="?android:listPreferredItemPaddingEnd"
    android:paddingStart="?android:listPreferredItemPaddingStart" />
```

### Login

To keep track of when a user accepted the terms of service you need to update the saved timestamp by calling one of the provided methods. This information is used to infer whether something changed since it was last seen by the user.

```kt
GDPRPolicyManager.instance().setPolicyAccepted(true) // if local, uses current timestamp
GDPRPolicyManager.instance().setPolicyAccepted(timestamp) // queried from server
```

### Policy Changes

You should query your server about changes to the policy. Call `manager.updateLatestPolicyTimestamp(timestamp)` to update the saved timestamp of the last policy changes. You could do this at every app start, or from a background job.

#### Notify the User

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

#### Send Updates to the Server

The library will send broadcasts whenever something changes, e.g. when the user

1. accepts the policy, either from calling `setPolicyAccepted` or when the user accepts the new policy from the dialog
2. enables or disables a service

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

In this receiver you should send or queue the updates to the server as well as start/stop your tracking services.
