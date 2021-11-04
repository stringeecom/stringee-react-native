
# stringee-react-native

## Getting started

### Installation

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
    ```
## Version
- 1.4.10: Allow change base stringeeXUrl.
