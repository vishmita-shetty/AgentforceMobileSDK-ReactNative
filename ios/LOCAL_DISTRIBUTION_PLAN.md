# Local Distribution Plan for AgentforceSDK and AgentforceService

## Overview

This document outlines the plan for creating local XCFramework distributions of AgentforceSDK and AgentforceService for testing custom changes before CI distribution.

## Distribution Repository Analysis

### AgentforceService Distribution Pattern
- **Repository**: `https://github.com/forcedotcom/AgentforceMobileService-iOS`
- **Release naming**: `AgentforceMobileService-256-3.xcframework.zip`
- **Podspec location**: `SalesforceMobileSDK-iOS-Specs/AgentforceService/{version}/AgentforceService.podspec`
- **Distribution mechanism**: XCFramework zip via GitHub releases
- **Podspec pattern**:
  ```ruby
  s.source = { :http => 'https://github.com/forcedotcom/AgentforceMobileService-iOS/releases/download/3.2.0/AgentforceMobileService-256-3.xcframework.zip' }
  s.vendored_framework = 'AgentforceService.xcframework'
  ```

### AgentforceSDK Distribution Pattern
- **Repository**: `https://github.com/salesforce/AgentforceMobileSDK-iOS`
- **Release naming**: `AgentforceMobileSDK-iOS-256-3.xcframework.zip`
- **Latest version**: 11.6.0 (tag), corresponds to "256.3" internal build
- **Podspec location**: `SalesforceMobileSDK-iOS-Specs/AgentforceSDK/{version}/AgentforceSDK.podspec`
- **Distribution mechanism**: XCFramework zip via GitHub releases
- **Podspec pattern**:
  ```ruby
  s.source = { :http => 'https://github.com/salesforce/AgentforceMobileSDK-iOS/releases/download/11.6.0/AgentforceMobileSDK-iOS-256-3.xcframework.zip' }
  s.vendored_framework = 'AgentforceSDK.xcframework'
  ```

### Key Differences from Source Build
- **Dependencies**: Distribution podspecs include `swift-markdown-ui` dependency (which we've removed in our custom implementation)
- **Version constraints**: Distribution uses `~> 3` for AgentforceService dependency vs our flexible resolution
- **File structure**: Uses `vendored_framework` pointing to XCFramework instead of `source_files`

## Local Distribution Plan

### Phase 1: XCFramework Creation
1. Build AgentforceService XCFramework for iOS Simulator + Device architectures
2. Build AgentforceSDK XCFramework for iOS Simulator + Device architectures  
3. Package each as zip files following naming convention

### Phase 2: Local Podspec Creation
1. Create local podspecs that mirror distribution structure but point to local file paths
2. Remove `swift-markdown-ui` dependency from AgentforceSDK podspec
3. Use flexible version constraints matching our working solution

### Phase 3: Local Testing
1. Update ReactNative Podfile to use local XCFramework podspecs
2. Test `pod install` and build process
3. Verify our custom SalesforceMarkdown integration works with binary distribution

### Phase 4: Validation
1. Compare binary vs source build behavior
2. Ensure ReactCommon conflicts are resolved
3. Validate that all custom markdown functionality is preserved

## Build Artifact Locations

### Directory Structure
```
/Users/jbovet/Documents/GitHub/Syngenta/
├── AgentforceService/                    # Source code
│   ├── build/                           # Build artifacts
│   │   ├── ios.xcarchive/
│   │   └── ios-simulator.xcarchive/
│   ├── AgentforceService.xcframework    # Final XCFramework
│   ├── AgentforceMobileService-local.xcframework.zip
│   └── AgentforceService-local.podspec  # Local podspec
├── AgentforceSDK/                       # Source code  
│   ├── build/                           # Build artifacts
│   │   ├── ios.xcarchive/
│   │   └── ios-simulator.xcarchive/
│   ├── AgentforceSDK.xcframework        # Final XCFramework
│   ├── AgentforceMobileSDK-iOS-local.xcframework.zip
│   └── AgentforceSDK-local.podspec      # Local podspec
└── ReactNative-AgentforceSDK/ios/       # ReactNative project
    ├── Podfile                          # Updated to use local podspecs
    └── LOCAL_DISTRIBUTION_PLAN.md       # This document
```

## Implementation Steps

### Step 1: Build XCFrameworks

#### AgentforceService XCFramework
```bash
cd /Users/jbovet/Documents/GitHub/Syngenta/AgentforceService

# Create build directory
mkdir -p build

# Build for iOS device
xcodebuild archive \
  -workspace AgentforceService.xcworkspace \
  -scheme AgentforceService \
  -destination "generic/platform=iOS" \
  -archivePath "build/ios" \
  SKIP_INSTALL=NO \
  BUILD_LIBRARY_FOR_DISTRIBUTION=YES

# Build for iOS Simulator
xcodebuild archive \
  -workspace AgentforceService.xcworkspace \
  -scheme AgentforceService \
  -destination "generic/platform=iOS Simulator" \
  -archivePath "build/ios-simulator" \
  SKIP_INSTALL=NO \
  BUILD_LIBRARY_FOR_DISTRIBUTION=YES

# Create XCFramework in project root
xcodebuild -create-xcframework \
  -framework build/ios.xcarchive/Products/Library/Frameworks/AgentforceService.framework \
  -framework build/ios-simulator.xcarchive/Products/Library/Frameworks/AgentforceService.framework \
  -output "AgentforceService.xcframework"

# Create zip in project root
zip -r AgentforceMobileService-local.xcframework.zip AgentforceService.xcframework
```

#### AgentforceSDK XCFramework
```bash
cd /Users/jbovet/Documents/GitHub/Syngenta/AgentforceSDK

# Create build directory
mkdir -p build

# Build for iOS device
xcodebuild archive \
  -workspace AgentforceSDK.xcworkspace \
  -scheme AgentforceSDK \
  -destination "generic/platform=iOS" \
  -archivePath "build/ios" \
  SKIP_INSTALL=NO \
  BUILD_LIBRARY_FOR_DISTRIBUTION=YES

# Build for iOS Simulator
xcodebuild archive \
  -workspace AgentforceSDK.xcworkspace \
  -scheme AgentforceSDK \
  -destination "generic/platform=iOS Simulator" \
  -archivePath "build/ios-simulator" \
  SKIP_INSTALL=NO \
  BUILD_LIBRARY_FOR_DISTRIBUTION=YES

# Create XCFramework in project root
xcodebuild -create-xcframework \
  -framework build/ios.xcarchive/Products/Library/Frameworks/AgentforceSDK.framework \
  -framework build/ios-simulator.xcarchive/Products/Library/Frameworks/AgentforceSDK.framework \
  -output "AgentforceSDK.xcframework"

# Create zip in project root
zip -r AgentforceMobileSDK-iOS-local.xcframework.zip AgentforceSDK.xcframework
```

### Step 2: Create Local Podspecs

#### AgentforceService Local Podspec
Create `AgentforceService-local.podspec`:
```ruby
Pod::Spec.new do |s|
  s.name = 'AgentforceService'
  s.version = '3.2.0-local'
  s.summary = 'Local build of AgentforceService'
  s.homepage = 'https://github.com/forcedotcom/AgentforceMobileService-iOS'
  s.license = { :type => 'Salesforce' }
  s.author = { 'Mobile Agentforce SDK' => 'mobile-agentforcesdk-public@salesforce.com' }
  
  s.source = { :path => '/Users/jbovet/Documents/GitHub/Syngenta/AgentforceService' }
  s.vendored_framework = 'AgentforceService.xcframework'
  
  s.ios.deployment_target = '17.0'
  s.swift_versions = ['5.0', '5.1']
  
  s.dependency 'SalesforceNetwork'
  s.dependency 'SalesforceLogging'
end
```

#### AgentforceSDK Local Podspec
Create `AgentforceSDK-local.podspec`:
```ruby
Pod::Spec.new do |s|
  s.name = 'AgentforceSDK'
  s.version = '11.8.5-local'
  s.summary = 'Local build of AgentforceSDK with custom SalesforceMarkdown'
  s.homepage = 'https://github.com/salesforce/AgentforceMobileSDK-iOS'
  s.license = { :type => 'Salesforce' }
  s.author = { 'Mobile Agentforce SDK' => 'mobile-agentforcesdk-public@salesforce.com' }
  
  s.source = { :path => '/Users/jbovet/Documents/GitHub/Syngenta/AgentforceSDK' }
  s.vendored_framework = 'AgentforceSDK.xcframework'
  
  s.ios.deployment_target = '17.0'
  s.swift_versions = ['5.0', '5.1']
  
  # Note: NO swift-markdown-ui dependency - we use custom SalesforceMarkdown
  s.dependency 'AgentforceService'
  s.dependency 'SalesforceNavigation'
  s.dependency 'SalesforceLogging'
  s.dependency 'SalesforceNetwork'
  s.dependency 'SalesforceUser'
  s.dependency 'LiveKitClient'
end
```

### Step 3: Update ReactNative Podfile

Update `ReactNative-AgentforceSDK/ios/Podfile`:
```ruby
target 'ReactAgentforce' do
  source 'https://github.com/forcedotcom/SalesforceMobileSDK-iOS-Specs.git'
  source 'https://cdn.cocoapods.org/'
  source "https://github.com/livekit/podspecs.git"

  # Use local XCFramework distributions
  pod 'AgentforceSDK', :podspec => '../../AgentforceSDK/AgentforceSDK-local.podspec'
  pod 'AgentforceService', :podspec => '../../AgentforceService/AgentforceService-local.podspec'
  
  # Rest of configuration...
end
```

## Success Criteria

1. **XCFramework builds successfully** for both AgentforceSDK and AgentforceService
2. **ReactNative app builds** without ReactCommon conflicts using local XCFrameworks
3. **Custom SalesforceMarkdown** renders correctly in binary distribution
4. **Performance matches** source build implementation
5. **All tests pass** with local XCFramework distribution

## Implementation Results ✅

### Key Findings & Solutions

1. **CocoaPods Integration Challenge**: Initial attempts using `:path =>` still compiled from source rather than using true binary distribution
   - **Solution**: Created separate `-binary.podspec` files with `s.source = { :http => 'file://...' }` pointing to local zip files
   - **Result**: True binary distribution that exactly mirrors GitHub release pattern

2. **File URL Strategy**: Used `file://` URLs with `File.expand_path()` to reference local XCFramework zips
   - **AgentforceSDK**: `file:///Users/jbovet/Documents/GitHub/Syngenta/AgentforceSDK/AgentforceMobileSDK-iOS-local.xcframework.zip`
   - **AgentforceService**: `file:///Users/jbovet/Documents/GitHub/Syngenta/AgentforceService/AgentforceMobileService-local.xcframework.zip`

3. **Podfile Configuration**: Updated to use binary podspecs via `:podspec` parameter:
   ```ruby
   pod 'AgentforceSDK', :podspec => '../../AgentforceSDK/AgentforceSDK-binary.podspec'
   pod 'AgentforceService', :podspec => '../../AgentforceService/AgentforceService-binary.podspec'
   ```

4. **Dependency Management**: Maintained flexible dependency resolution (no version constraints) to prevent ReactCommon conflicts
5. **Custom SalesforceMarkdown**: Successfully integrated without external `swift-markdown-ui` dependency

### Verification Steps Completed ✅
- ✅ XCFrameworks built successfully for both SDKs
- ✅ CocoaPods installation successful with binary distribution
- ✅ Local zip files downloaded during `pod install`
- ✅ Podspec validation confirms vendored frameworks approach
- ✅ **ReactNative build completed successfully (BUILD SUCCEEDED)**
- ✅ Custom SalesforceMarkdown works correctly in binary distribution
- ✅ No ReactCommon module conflicts with binary frameworks

## Notes

- **Critical Insight**: The solution required true binary distribution simulation, not just conditional source builds
- **Distribution Accuracy**: Now perfectly mirrors customer experience with GitHub releases  
- **Testing Readiness**: Local setup enables thorough testing before CI submission
- **Flexible Dependencies**: Maintained conflict-free dependency resolution from previous work