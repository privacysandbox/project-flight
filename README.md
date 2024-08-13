# Project Flight: README

Project Flight is a sample project that combines the privacy-preserving APIs of
the Privacy Sandbox on Android to demonstrate how they can be used together in
end-to-end customer journeys.

Project Flight includes five components:

- Publisher app
    -   A sample app that represents a publisher in the advertising flow. This
        example publisher is a news app that shows an ad when the user scrolls
        through the content.
- Advertiser app
    -   A sample app that represents the advertiser in the advertising flow. This
        example advertiser is a travel app, selling experiences such as flights and
        hotels to various destinations.
-   SSP module
    -   This module represents an SSP SDK in the advertising flow. This module calls
        `registerSource()` for measurement, and runs ad selection from the Publisher
        app based on the Custom Audiences joined by the user.
-   MMP module
    -   This MMP represents an MMP SDK in the advertising flow. The module registers
        triggers for measurement and handles joining custom audiences inside of the
        advertiser app.
-   Firebase hosting specs
    -   To represent the ad tech backend in the advertising flow, these files can be
        used to run a local server and store the static files with Firebase Hosting.

### Get started

#### Set up your development environment

System requirements:

-   Android Studio Giraffe | 2022.3.1 RC 1 or higher
-   Gradle 8.1 or higher
-   An emulator or physical Pixel device running a [Tiramisu Privacy Sandbox (API
    level 33) extension 5][1] system image with Play Store enabled.

### Checkout the codebase

#### Set up your Firebase server

To use the functionality of the Protected Audiences and Attribution Reporting
APIs, you need to have server endpoints set up to receive calls from the Flight
client apps. There are existing resources for setting up [Protected Audience
servers][2] and [Attribution Reporting servers][3] using
Open API specs or mock servers. Project Flight demonstrates how to use Firebase
for hosting the server.

To set up your Firebase server, there are two different configuration options
provided in the "Firebase-Backend" folder: The first option only uses the
Firebase "Hosting" functionality, and the second has "Functions" enabled. The
difference between these two is that by using the hosting-only setup (inside the
"Without-Functions" folder) you can deploy the full Firebase server without
having to upgrade to a Blaze pay-as-you-go plan for Firebase. However, you also
will be unable to view the body of aggregate and event reports sent to server
endpoints with this setup. The "Functions-Enabled" folder contains a Firebase
server configuration which uses the Firebase Functions product, enabling the
report content to be viewed in full by calling a function to log the body on
receipt.

_Note: the Firebase Blaze plan is pay-as-you-go, and setup using the
"Functions-Enabled" configuration will still be free so long as Firebase monthly
usage limits are adhered to. [Learn more about billing plans][4]._

1.  To begin, follow the instructions for [Getting Started with Firebase
    Hosting][5]. When given the option to select a local directory for
    your firebase project, select either the "Without-Functions" or
    "Functions-Enabled" folder inside of the `Firebase-Backend` directory
    of the repository you've just cloned.
1.  Once you have your Firebase project set up and your desired configuration
    designated as your local reference for that project, deploy your server
    according to the instructions on that doc. You should now have your server
    endpoints for Attribution and Protected Audiences available to be called from
    the client app. For support in using Firebase Functions, [documentation is
    available][6] from the Firebase team to help with getting started
    and expanding the available functionality.
1.  Replace the `topLevelDomain` variables inside of the project code to match
    the URL you've now created for your Firebase project. This variable is present
    in two places:
    1.  Inside of the [SspSdKImpl.kt][16] file in the `SspSdk` project.
    1.  Inside of the [MmpSdkImpl.kt][15] file in the `MmpSdk` project.
    1.  If you want to point to a different server for Attribution (such as one
        that has already been set up and enrolled for testing the [measurement
        sample app][7], you can do so by replacing the following variables:
        - `registerSourceUrl` inside of [SspSdkImpl.kt][8]
        - `registerTriggerUrl` inside of [MmpSdkImpl.kt][9]
1.  To monitor calls being sent to the Firebase server, go to
    [https://console.cloud.google.com/logs/][10] and select your
    project. If you are using the "Without-Functions" version of the setup, when
    receiving event and aggregate reports you will not be able to view the content
    of the reports, but you will still see the calls being sent to the reporting
    endpoints on the server.

#### Set up Privacy Sandbox on your device

Open up the AdvertiserApp or PublisherApp projects in Android Studio and start
your emulator. Follow these instructions to set up your emulator to run the
project.

First, toggle the Privacy Sandbox to "On" in Settings. You can navigate to the
correct screen with this command:

```
adb shell am start -n com.google.android.adservices.api/com.android.adservices.ui.settings.activities.AdServicesSettingsMainActivity
```

Next, set permissions. These permissions should be set every time you run the
project, even if you have set them before. To set these permissions, run the
following commands:

```
adb shell device_config set_sync_disabled_for_tests persistent
adb shell device_config put adservices ppapi_app_signature_allow_list \"\*\"
adb shell device_config put adservices ppapi_app_allow_list \"\*\"
adb shell device_config put adservices adservice_system_service_enabled true
adb shell device_config put adservices adservice_enabled true
adb shell device_config put adservices adservice_enable_status true
adb shell device_config put adservices global_kill_switch false
adb shell device_config put adservices measurement_kill_switch false
adb shell device_config put adservices fledge_js_isolate_enforce_max_heap_size false
adb shell device_config put adservices fledge_custom_audience_service_kill_switch false
adb shell device_config put adservices fledge_select_ads_kill_switch false
adb shell device_config put adservices adid_kill_switch false
adb shell device_config put adservices disable_fledge_enrollment_check true
adb shell device_config put adservices fledge_register_ad_beacon_enabled true
adb shell device_config put adservices fledge_measurement_report_and_register_event_api_enabled true
adb shell device_config put adservices disable_measurement_enrollment_check true
```

You can copy and paste the entire block of commands above into your terminal and
they will each execute in turn.

### Enroll your project

If you haven't done so already for your project, you must enroll your site URL
to use Protected Audiences (e.g. `https://example.com`) and your source and
trigger URLs to use Attribution Reporting (e.g.
`https://example.com/register_source`). Read the [enrollment
documentation][11] to begin the enrollment process.

Once you have completed enrollment, you will receive an enrollment URL and
enrollment ID that you can use to set up your project in the following steps.

1.  Add your enrollment URL to your device with this command:

    ```
    adb shell device_config put adservices mdd_measurement_manifest_file_url <your-enrollment-url>
    ```
1.  Verify your enrollment URL with this command:

    ```
    adb shell device_config get adservices mdd_measurement_manifest_file_url
    ```
1.  Run the Publisher App, or make a call with any of the Privacy Sandbox APIs
    (e.g. `registerSource()` or `joinCustomAudience()`). **This call is expected
    to fail**.
1.  Force the enrollment job with this command:

    ```
    adb shell cmd jobscheduler run -f com.google.android.adservices.api 14
    ```
1.  Replace the enrollment ID in the [ad_services_config][12] file in the [MMP
    SDK Module][13] and the [SSP SDK Module][14]
1.  Make sure the URLs in the project to match the urls that you have enrolled
    1.  In the MMP SDK Module:
        1. Make sure [`topLevelDomain`][15] matches your site
        1. Make sure [`registerTriggerUrl`][9] matches your enrolled URL
        for `registerTrigger()`
    1. In the SSP SDK Module:
        1.  Make sure [`topLevelDomain`][16] matches your site
        1.  Make sure [`registerSourceUrl`][8] matches your enrolled URL
            for `registerSource()`
1.  If you have followed all instructions and are getting a `Caller not
    authorized` error, try restarting the device.

### Running the use cases

#### Select and show an Ad

1.  Open the AdvertiserApp project.
1.  Click either "Athens" or "Cairo" to add you to a custom audience for the
    given location. (Clicking Berlin or Delhi will not add you to a custom
    audience.)
1.  Open the PublisherApp project
1.  See the ad for the place you chose appear

#### Attributing an ad

1.  Make sure an ad is showing by following the steps in the prior "Select and
    show an Ad" section. You can't attribute an ad that isn't shown.
1.  Open PublisherApp, and click on the ad that is shown. You will see a toast
    appear that indicates if the source registration was successful.
1.  Run the job to record the registration on device
    1.  For Tiramisu (API level 33) and lower, run

        ```
        adb shell cmd jobscheduler run -f com.google.android.adservices.api 15
        ```
    1.  For UpsideDownCake (API level 34) and higher, run

    ```
    adb shell cmd jobscheduler run -f com.google.android.adservices.api 20
    ```
1.  Open AdvertiserApp, and navigate to a product detail page (e.g. click the
    button that says "Athens")
1.  Click the "Book Now" button. You will see a toast appear that will indicate
    if the trigger registration was successful
1.  Run the job to record the registration on device
    1.  For Tiramisu (API level 33) and below, run

        ```
        adb shell cmd jobscheduler run -f com.google.android.adservices.api 15
        ```
    1.  For UpsideDownCake (API level 34) and above, run

        ```
        adb shell cmd jobscheduler run -f com.google.android.adservices.api 20
        ```
1.  Force the AttributionJobService

    ```
    adb shell cmd jobscheduler run -f com.google.android.adservices.api 5
    ```
1.  On your device, open the time and date settings. Disable automatic time and
    date. Then, set the date to three days forward.
1.  Force the ReportingJobService to send Event Level and Aggregate reports

    ```
    adb shell cmd jobscheduler run -f com.google.android.adservices.api 3
    ```

    ```
    adb shell cmd jobscheduler run -f com.google.android.adservices.api 7
    ```
1.  Check the server logs to verify receipt of Event Level and Aggregate reports.
    _Note: If you are using the "Functions" Firebase setup, you will be able to
    see the content of the reports in the logs. If you are using
    "Without-Functions," you will be able to verify receipt of the reports, but
    not be able to see their contents_.

[1]: https://developer.android.com/design-for-safety/privacy-sandbox/setup-device-access?version=beta
[2]: https://github.com/android/privacy-sandbox-samples/tree/main/Fledge/FledgeServerSpec
[3]: https://github.com/android/privacy-sandbox-samples/tree/main/AttributionReporting
[4]: https://firebase.google.com/pricing
[5]: https://firebase.google.com/docs/hosting/quickstart
[6]: https://firebase.google.com/docs/functions/get-started?gen=1st
[7]: https://github.com/android/privacy-sandbox-samples/tree/main/AttributionReporting
[8]: https://github.com/privacysandbox/project-flight/blob/main/SspSdk/SspModule/src/main/java/com/example/sspsdk/SspSdkImpl.kt#L70
[9]: https://github.com/privacysandbox/project-flight/blob/main/MmpSdk/MmpModule/src/main/java/com/example/mmpsdk/MmpSdkImpl.kt#L72
[10]: https://console.cloud.google.com/logs/
[11]: https://github.com/privacysandbox/attestation/blob/main/how-to-enroll.md
[12]: https://developer.android.com/design-for-safety/privacy-sandbox/setup-api-access?version=beta#configure-api-specific
[13]: https://github.com/privacysandbox/project-flight/blob/main/MmpSdk/MmpModule/src/main/res/xml/ad_services_config.xml
[14]: https://github.com/privacysandbox/project-flight/blob/main/SspSdk/SspModule/src/main/res/xml/ad_services_config.xml
[15]: https://github.com/privacysandbox/project-flight/blob/main/MmpSdk/MmpModule/src/main/java/com/example/mmpsdk/MmpSdkImpl.kt#L55
[16]: https://github.com/privacysandbox/project-flight/blob/main/SspSdk/SspModule/src/main/java/com/example/sspsdk/SspSdkImpl.kt#L49
