# ReactNative AgentforceSDK - Customer Distribution

This is a complete ReactNative application with embedded AgentforceSDK and AgentforceService frameworks.

## Quick Start

1. **Prerequisites:**
   - iOS 17.0+ device or simulator
   - Xcode 15.0+
   - CocoaPods 1.11+
   - Node.js 18+

2. **Setup:**
   ```bash
   # Navigate to iOS directory
   cd ios

   # Install iOS dependencies
   pod install

   # Open the workspace (NOT the .xcodeproj)
   open ReactAgentforce.xcworkspace
   ```

3. **Run the app:**
   - Select your target device/simulator in Xcode
   - Press Cmd+R to build and run

## What's Included

- **Complete ReactNative App:** Ready-to-run AgentforceSDK demo application
- **Embedded Frameworks:** Pre-built AgentforceSDK and AgentforceService XCFrameworks
- **All Dependencies:** Configured with proper Salesforce Mobile SDK integration

## Project Structure

```
ReactNative-AgentforceSDK/
├── ios/
│   ├── Frameworks/                  # Embedded binary frameworks
│   │   ├── AgentforceSDK/
│   │   └── AgentforceService/
│   ├── Podfile                     # CocoaPods configuration
│   └── ReactAgentforce.xcworkspace  # Open this in Xcode
├── android/                        # Android project files
├── app.tsx                        # Main React Native app
└── package.json                   # Node.js dependencies
```

## Troubleshooting

### Pod Install Issues
If `pod install` fails:
```bash
cd ios
pod cache clean --all
pod install --repo-update
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

For technical questions about AgentforceSDK integration, please refer to the Salesforce Mobile SDK documentation.

---

Generated with Syngenta build system - Tue Sep 16 18:25:41 CEST 2025
