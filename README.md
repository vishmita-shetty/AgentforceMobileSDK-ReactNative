# ReactNative AgentforceSDK - Customer Distribution

This is a complete ReactNative application with embedded AgentforceSDK and AgentforceService frameworks, featuring a customer-deployable data provider implementation without any internal Salesforce framework dependencies.

## Quick Start

### Prerequisites
- iOS 17.0+ device or simulator
- Xcode 15.0+
- Node.js 18+
- Yarn or npm

### Setup (One Command!)
```bash
# Install all dependencies (Node modules, Mobile SDK, and CocoaPods)
node installios.js
```

### Run the app
```bash
# Open the workspace in Xcode
open ios/ReactAgentforce.xcworkspace
```
- Select your target device/simulator in Xcode
- Press Cmd+R to build and run

## What's Included

- **Complete ReactNative App:** Ready-to-run AgentforceSDK demo application
- **Embedded Frameworks:** Pre-built AgentforceSDK and AgentforceService XCFrameworks
- **All Dependencies:** Configured with proper Salesforce Mobile SDK integration
- **One-Step Setup:** The `installios.js` script handles all installation automatically
- **Customer-Ready:** No internal framework dependencies - suitable for customer production deployments

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

## What installios.js Does

The setup script automatically:
1. Installs all Node.js dependencies with Yarn
2. Clones the Salesforce Mobile SDK to the correct location
3. Configures Xcode environment variables
4. Installs and updates all CocoaPods dependencies

## Development

### Starting React Native Server
```bash
npm start
```

### Testing
Build and run the iOS simulator to test the complete Agentforce integration.

## Troubleshooting

### Setup Issues
If `node installios.js` fails:
```bash
# Clean and retry
rm -rf node_modules ios/Pods mobile_sdk
node installios.js
```

### Build Issues
If Xcode build fails:
1. Clean build folder: Product > Clean Build Folder
2. Delete DerivedData: `rm -rf ~/Library/Developer/Xcode/DerivedData`
3. Try building again

### Framework Issues
The embedded frameworks are pre-built with:
- iOS 17.0+ deployment target
- BUILD_LIBRARY_FOR_DISTRIBUTION=YES
- Compatible with arm64 (device) and x86_64 + arm64 (simulator)

## Support

This implementation provides a complete, customer-deployable Agentforce solution without any internal Salesforce framework dependencies. All components use public APIs and are suitable for customer production deployments.

For technical questions about AgentforceSDK integration, please refer to the Salesforce Mobile SDK documentation.