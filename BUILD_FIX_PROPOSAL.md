# iOS Build Fix Proposal

## Issue
**Error**: `/Users/jbovet/Documents/GitHub/ReactNative-AgentforceSDK/node_modules/react-native/ReactCommon/yoga/yoga/config/Config.h:10:10: error: 'bitset' file not found (in target 'cmark_gfm' from project 'Pods')`

## Context
- **Project**: ReactNative-AgentforceSDK
- **React Native Version**: 0.74.7
- **Salesforce Mobile SDK**: v13.0.2
- **iOS Deployment Target**: 17.0
- **Error Location**: React Native's yoga layout engine, affecting cmark_gfm pod
- **Platform**: iOS build

## Current Podfile Analysis

### Existing Configuration
The current Podfile (`ios/Podfile:60-65`) already has a `post_install` hook that sets:
```ruby
post_install do |installer|
  # ... other configurations ...
  
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['BUILD_LIBRARY_FOR_DISTRIBUTION'] = 'YES'
    end
  end
end
```

### Salesforce Mobile SDK Integration
- Uses `mobile_sdk_pre_install` and `mobile_sdk_post_install` hooks from `/mobile_sdk/SalesforceMobileSDK-iOS/mobilesdk_pods.rb`
- Targets iOS 17.0 deployment for Mobile SDK components
- Configures dynamic frameworks for Salesforce SDK components
- Already includes AgentforceSDK pod from Salesforce specs

## Root Cause
This is a React Native 0.74+ issue where C++ standard library headers aren't being found during iOS builds. The yoga layout engine requires C++17 standard library features including `<bitset>`, but the current Podfile doesn't specify C++ language standards for all targets.

## Proposed Solution

### Step 1: Update Existing Podfile Configuration
Modify the existing `post_install` block in `ios/Podfile` (lines 60-65) to include C++ settings:

```ruby
installer.pods_project.targets.each do |target|
  target.build_configurations.each do |config|
    config.build_settings['BUILD_LIBRARY_FOR_DISTRIBUTION'] = 'YES'
    # Add C++ configuration for React Native compatibility
    config.build_settings['CLANG_CXX_LANGUAGE_STANDARD'] = 'c++17'
    config.build_settings['CLANG_CXX_LIBRARY'] = 'libc++'
  end
end
```

### Step 2: Clean and Reinstall Dependencies
```bash
cd ios
rm -rf Pods Podfile.lock
pod install
```

### Step 3: Fallback - Manual Xcode Project Settings
If the Podfile fix doesn't resolve the issue, manually set in Xcode:
- **C++ Language Dialect**: C++17
- **C++ Standard Library**: libc++

## Why This Will Work
1. **Targeted Fix**: Addresses the specific issue where React Native 0.74's yoga engine can't find C++ standard library headers
2. **Minimal Impact**: Only adds two build settings to existing configuration, doesn't interfere with Salesforce SDK setup
3. **Standard Solution**: This is the established fix for this React Native + CocoaPods issue
4. **Compatible**: Works with existing Mobile SDK post-install hooks and AgentforceSDK integration

## Expected Outcome
- Resolves C++ header not found errors in yoga/cmark_gfm
- Enables successful iOS builds  
- Maintains compatibility with Salesforce Mobile SDK v13.0.2 and AgentforceSDK
- Preserves existing Salesforce SDK configuration (dynamic frameworks, deployment targets)
- No impact on Android builds

## Implementation Priority
**High** - This blocks iOS development entirely and is a well-documented React Native 0.74+ configuration issue with an established fix.