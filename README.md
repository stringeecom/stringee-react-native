
# stringee-react-native

## Getting started

### Installation
For Expo project you need to enable and generate the native code in your project by running 

`$ npx expo prebuild`

[More detail about Expo prebuild](https://docs.expo.dev/workflow/prebuild/)

Install `stringee-react-native` by running:

`$ npm install stringee-react-native --save`

#### iOS

**Note** Please make sure to have [CocoaPods](https://cocoapods.org/) on your computer.
1. In you terminal, change into your `ios` directory.

2. Create a pod file by running: `pod init`.

3. Add the following to your pod file:

```
    platform :ios, '8.0'

    target '<YourProjectName>' do
      node_modules_path = '../node_modules'

      pod 'yoga', path: "#{node_modules_path}/react-native/ReactCommon/yoga/yoga.podspec"
      pod 'React', path: "#{node_modules_path}/react-native", :subspecs => ['DevSupport', 'RCTNetwork']

      pod 'RNStringee', path: "#{node_modules_path}/stringee-react-native/ios"
    end

    post_install do |installer|
      installer.pods_project.targets.each do |target|
        if target.name == "React"
          target.remove_from_project
        end
      end
    end

```

4. Now run, `pod install`

5. Open XCode

6. Open `<YourProjectName>.xcworkspace` file in XCode. This file can be found in the `ios` folder of your React Native project. 

7. In the "Build Settings" tab -> "Other linker flags" add "$(inherited)" flag.

8. In the "Build Settings" tab -> "Enable bitcode" select "NO".

9. Right-click the information property list file (Info.plist) and select Open As -> Source Code.

10. Insert the following XML snippet into the body of your file just before the final element:

```
  <key>NSCameraUsageDescription</key>
  <string>$(PRODUCT_NAME) uses Camera</string>
  <key>NSMicrophoneUsageDescription</key>
  <string>$(PRODUCT_NAME) uses Microphone</string>
```

#### Android

##### Proguard
Open up `android/app/proguard-rules.pro` and add following lines: 
```
-dontwarn org.webrtc.**
-keep class org.webrtc.** { *; }
-keep class com.stringee.** { *; }
```

##### Permissions
The Stringee Android SDK requires some permissions from your AndroidManifest
1. Open up `android/app/src/main/AndroidManifest.xml`
2. Add the following lines:
    ```
    // for internet access
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    // for audio access
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    // for camera access
    <uses-permission android:name="android.permission.CAMERA" />
    // for bluetooth 
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    ```

##### Dependencies
1. Open up `android/app/build.gradle`
2. Add the following lines:
    ```
    dependencies {
    ...
    implementation 'com.android.volley:volley:*.*.*'
    ...
    }
    ```

### Version 1.5.8
##### Features:
- Bug fixed: Connectivity doesn't establish with DualSIM iPhones.

### Version 1.5.7
##### Features:
- Allow setting ScaleType for StringeeVideoView.

### Version 1.5.6
##### Features:
- Bug fixed: Crash on checking exist call progress.

### Version 1.5.5
##### Bug fixed:
- Reset user information after reconnecting.

### Version 1.5.4
##### Bug fixed:
- Ios get last message always empty.

### Version 1.5.3
##### Features:
- Add event delete message when send message error.

### Version 1.5.2
##### Features:
- Upgrade webrtc android version.

### Version 1.5.1
##### Bug fixes:
- Convert wrong user data from native.
##### Features:
- Upgrade ios sdk version.

### Version 1.5.0
##### Bug fixes:
- Declare ViewPropTypes from react native version 0.69.0.
##### Features:
- Allow to get user info and update user info.
- Upgrade android sdk version.
- Upgrade ios sdk version.

### Version 1.4.31
##### Features:
- Allow to snapshot screen in eykc.

### Version 1.4.30
##### Features:
- Allow to send trackMediaStateChangeEvent.

### Version 1.4.29
##### Bug fixes:
- Missing listener in android.

### Version 1.4.28
##### Upgrade sdk:
- Upgrade android sdk.

### Version 1.4.27
##### Bug fixes:
- Wrong error value in android.

### Version 1.4.26
##### Features:
- Allows deleting tokens on other devices by package name when registerPush.

### Version 1.4.25
##### Features:
- Allows deleting tokens on other devices when registerPush.
   
### Version 1.4.24
##### Bug fixes:
- No event when user is removed from conversation

### Version 1.4.23
##### Bug fixes:
- Return the user's id instead of the user's name in android messages

### Version 1.4.22
##### Bug fixes:
- Return wrong data type in callback of function getMessageById

### Version 1.4.21
##### Bug fixes:
- Add getMessageById function

### Version 1.4.20
##### Bug fixes:
- Can not render video in background iOS

### Version 1.4.19
##### Features:
- Allow edit message, revoke message, pin/unpin message.

### Version 1.4.18
##### Bug fixes:
- No callback for deleteConversation and removeParticipant functions

### Version 1.4.15
##### Bug fixes:
- Fail to set stringeeXBaseUrl.

### Version 1.4.14
##### Bug fixes:
- No callback for hangup and reject functions.

### Version 1.4.13
##### Bug fixes:
- No call waiting video for StringeeCall 2

### Version 1.4.11
##### Bug fixes:
- No callback for markMessageAsSeen function.
    
### Version 1.4.10
##### Features:
- Allow stringeeXUrl base change.
