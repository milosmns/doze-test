Testing Doze
============
This app tries to simulate various scenarios related to Android's _Doze_ mode. It logs events to the console in equal time frames during its execution so that you can check whether the device actually went into Doze or not. Note: This app is written in Kotlin, don't get surprised by the syntax.

Understanding Doze
------------------
Starting from Android 6.0 (API level 23), Android introduces two power-saving features that extend battery life for users by managing how apps behave when their devices are not connected to a power source. _Doze_ reduces battery consumption by deferring background CPU and network activity for apps when the device is unused for long periods of time. _App Standby_ defers background network activity for apps with which the user has not recently interacted.

_Doze_ and _App Standby_ manage the behavior of all apps running on Android 6.0 or higher, regardless whether they are specifically targeting API level 23 or not. To ensure the best experience for users, developers need to test their apps in _Doze_ and _App Standby_ modes and make any necessary adjustments to their code.

If a user leaves a device **unplugged** and **stationary** for a period of time, with the screen off, the device enters Doze mode. In Doze mode, the system attempts to conserve battery by restricting apps' access to network and CPU-intensive services. It also prevents apps from accessing the network and defers their jobs, syncs, and standard alarms. Periodically, the system exits Doze for a brief time to let apps complete their deferred activities. During this **maintenance** window, the system runs all pending syncs, jobs, and alarms, and lets apps access the network. At the conclusion of each maintenance window, the system again enters Doze, suspending network access and deferring jobs, syncs, and alarms. Over time, the system schedules maintenance windows less and less frequently, helping to reduce battery consumption in cases of longer-term inactivity when the device is not connected to a charger. As soon as the user wakes the device by moving it, turning on the screen, or connecting a charger, the system exits Doze and all apps return to normal activity. Check the graphic for more info, or see the [explanation video](https://www.youtube.com/watch?v=N72ksDKrX6c).

![Doze Activity](http://i.imgur.com/G0o7OWw.png)

Google Cloud Messaging and waking up
------------------------------------
**Google Cloud Messaging** (GCM) is a cloud-to-device service that lets you support real-time downstream messaging between backend services and apps on Android devices. GCM provides a single, persistent connection to the cloud; all apps needing real-time messaging can share this connection. This shared connection significantly optimizes battery consumption by making it unnecessary for multiple apps to maintain their own, separate persistent connections, which can deplete the battery rapidly. GCM is optimized to work with Doze and App Standby idle modes by means of high-priority GCM messages. 

GCM high-priority messages let you reliably wake your app to access the network, even if the user’s device is in Doze or the app is in App Standby mode. In Doze or App Standby mode, the system delivers the message and gives the app temporary access to network services and partial wakelocks, then returns the device or app to idle state. High-priority GCM messages do not otherwise affect Doze mode, and they don’t affect the state of any other app. This means that any app can use them to communicate efficiently while minimizing battery impacts across the system and device. As a general best practice, if your server and client already use GCM, make sure that your service uses high-priority messages for critical messages, since this will reliably wake apps even when the device is in Doze.

Exceptions and whitelisting
---------------------------
Almost all apps should be able to support Doze by managing network connectivity, alarms, jobs, and syncs properly, and using GCM high-priority messages. For a narrow set of use cases, this might not be sufficient. For such cases, the system provides a configurable whitelist of apps that are partially exempt from Doze and App Standby optimizations.

An app that is **whitelisted** can use the network and hold partial wake locks during Doze and App Standby. However, other restrictions still apply to the whitelisted app, just as they do to other apps. For example, the whitelisted app’s jobs and syncs are deferred (on API level 23 and below), and its regular `AlarmManager` alarms do not fire. An app can check whether it is currently on the exemption whitelist by calling `isIgnoringBatteryOptimizations()` from the `PowerManager`.

Users can manually configure the whitelist in `Settings > Battery > Battery Optimization`. Alternatively, the system provides ways for apps to ask users to whitelist them. An app holding the `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` permission can trigger a system dialog to let the user add the app to the whitelist directly, without going to settings. The app fires a `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` Intent to trigger the dialog. The user can manually remove apps from the whitelist as needed. Before asking the user to add your app to the whitelist, developers need to make sure the app matches the [acceptable use cases](https://developer.android.com/training/monitoring-device-state/doze-standby.html#whitelisting-cases) for whitelisting.

Testing fundamentals
--------------------
You can test Doze mode by following these steps:

1. Configure a hardware device or virtual device with an Android 6.0 (API level 23) or higher system image
2. Connect the device to your development machine and install your app
3. Run your app and leave it active
4. Shut off the device screen (the app remains active)
5. Make the OS think your device is not connected to a charger by running the following command:
```bash
adb shell dumpsys battery unplug
```
6. Force the system to cycle through Doze modes by running the following command, you may need to run it more than once. Repeat it until the device state changes to `IDLE`.
```bash
adb shell dumpsys deviceidle step
```
Observe the behavior of your app after you reactivate the device. Make sure the app recovers gracefully when the device exits Doze. To test GCM, you can use any demo server you find, for example http://apns-gcm.bryantan.info/ or http://www.pushwatch.com/gcm/. Tokens should be acquired from the app developer.