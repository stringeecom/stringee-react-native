
# stringee-react-native

## Getting started

`$ npm install stringee-react-native --save`

### Mostly automatic installation

`$ react-native link stringee-react-native`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `stringee-react-native` and add `RNStringee.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNStringee.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNStringeePackage;` to the imports at the top of the file
  - Add `new RNStringeePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':stringee-react-native'
  	project(':stringee-react-native').projectDir = new File(rootProject.projectDir, 	'../node_modules/stringee-react-native/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':stringee-react-native')
  	```

## Usage
```javascript
import RNStringee from 'stringee-react-native';

// TODO: What to do with the module?
RNStringee;
```
  