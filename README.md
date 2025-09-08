# ReactNative-AgentforceSDK
AgentforceSDK integrated into a React Native application

Create ReactNative application
$ forcereact create --platform=ios,android --apptype=react_native_typescript --appname=ReactAgentforce --packagename=com.salesforce --organization=Salesforce --outputdir=

Start React Native server
$ npm start

Now you can open and run the React Native into the iOS simulator.

Next, let's add AgentforceSDK dependency as described in https://github.com/salesforce/AgentforceMobileSDK-iOS.

$ cd ios && pod install

[!] The following Swift pods cannot yet be integrated as static libraries:

The Swift pod `swift-markdown-ui` depends upon `cmark_gfm`, which does not define modules. To opt into those targets generating module maps (which is necessary to import them from Swift when building as static libraries), you may set `use_modular_headers!` globally in your Podfile, or specify `:modular_headers => true` for particular dependencies.


