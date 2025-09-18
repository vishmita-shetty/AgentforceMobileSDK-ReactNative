# ReactNative-AgentforceSDK

AgentforceSDK integrated into a React Native application with customer-deployable data provider implementation.

## Quick Start

### 1. Install iOS Dependencies
```bash
node installios.js
```

### 2. Open Xcode Workspace
```bash
open ios/ReactAgentforce.xcworkspace
```

### 3. Run in iOS Simulator
- Select your target device in Xcode
- Press `Cmd + R` to build and run

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

## Files Structure

```
ios/ReactAgentforce/Agentforce/
├── AgentforceDataProvider.swift          # Main data provider implementation
├── AgentforceCacheProvider.swift         # In-memory cache implementation
├── AgentforceClientManager.swift         # Swift AgentforceSDK integration
├── AgentforceManager.h/.m                # React Native bridge (Objective-C)
├── AgentforceAuthenticationProvider.swift # Authentication provider
├── AgentforceNetworkProvider.swift       # Network provider
├── AgentforceLoggingService.swift        # Logging service
├── AgentforceNavigationService.swift     # Navigation service
└── AgentforceUICoordinator.swift         # UI coordination
```

## Development

### Starting React Native Server
```bash
npm start
```

### Testing
Build and run the iOS simulator to test the complete Agentforce integration.

## Support

This implementation provides a complete, customer-deployable Agentforce solution without any internal Salesforce framework dependencies. All components use public APIs and are suitable for customer production deployments.