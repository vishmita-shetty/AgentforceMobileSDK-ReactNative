# ReactNative AgentforceSDK - Customer Distribution

This is a complete ReactNative application with embedded AgentforceSDK and AgentforceService frameworks, featuring a customer-deployable data provider implementation without any internal Salesforce framework dependencies.

## Quick Start

### iOS Setup
```bash
# Install all dependencies (Node modules, Mobile SDK, and CocoaPods)
node installios.js
```

### iOS Run
```bash
# Open the workspace in Xcode
open ios/ReactAgentforce.xcworkspace
```
- Select your target device/simulator in Xcode
- Press Cmd+R to build and run

### Android Setup
```bash
# Install all dependencies (Node modules and Mobile SDK)
node installandroid.js
```

### Android Run
```bash
# Run the app on connected device or emulator
npx react-native run-android
```
- Make sure you have an Android device connected via USB (with USB debugging enabled) or an Android emulator running
- Check connected devices with: `adb devices`

## React Native Integration

The project includes `AgentforceManager` - a React Native bridge that exposes Agentforce functionality to JavaScript:

```javascript
import { NativeModules } from 'react-native';
const { AgentforceManager } = NativeModules;

// Initialize Agentforce
AgentforceManager.initialize(agentId, orgId, endpoint, (error) => {
  if (error) {
    console.error('Agentforce initialization failed:', error);
  } else {
    console.log('Agentforce initialized successfully');
  }
});

// Present chat view
AgentforceManager.presentChatView(agentId, (error) => {
  if (error) {
    console.error('Failed to present chat:', error);
  }
});
```

## Project Structure

```
ReactNative-AgentforceSDK/
├── ios/
│   ├── Frameworks/                       # Embedded binary frameworks
│   │   ├── AgentforceSDK/
│   │   └── AgentforceService/
│   ├── ReactAgentforce/Agentforce/       # Swift implementation
│   │   ├── AgentforceDataProvider.swift          # Main data provider implementation
│   │   ├── AgentforceCacheProvider.swift         # In-memory cache implementation
│   │   ├── AgentforceClientManager.swift         # Swift AgentforceSDK integration
│   │   ├── AgentforceManager.h/.m                # React Native bridge (Objective-C)
│   │   ├── AgentforceAuthenticationProvider.swift # Authentication provider
│   │   ├── AgentforceNetworkProvider.swift       # Network provider
│   │   ├── AgentforceLoggingService.swift        # Logging service
│   │   ├── AgentforceNavigationService.swift     # Navigation service
│   │   └── AgentforceUICoordinator.swift         # UI coordination
│   ├── Podfile                          # CocoaPods configuration
│   └── ReactAgentforce.xcworkspace       # Open this in Xcode
├── android/                             # Android project files
├── app.tsx                             # Main React Native app
├── installios.js                       # One-step iOS setup script
└── package.json                        # Node.js dependencies
```

## Development

### Starting React Native Server
```bash
npm start
```

### Testing
Build and run the iOS simulator or Android emulator to test the complete Agentforce integration.
