# Binary Swift Framework Distribution: Complete Solution

## **Objective**
Enable React Native application to consume AgentforceSDK and AgentforceService as binary XCFrameworks instead of source pods, maintaining all functionality while improving build performance.

## **Problem Statement**
Runtime symbol resolution errors when consuming Swift frameworks as binary XCFrameworks:
```
dyld: Symbol not found: _$s17SalesforceLogging6LoggerP3log_5levelySS_AA8LogLevelOtFTj
dyld: Symbol not found: _$s20SalesforceNavigation0B0P2go2toyAA11Destination_p_tFTj
dyld: Symbol not found: _$s13LiveKitClient11ParticipantC8identityAC8IdentityCSgvgTj
```

## **Root Cause Analysis**

### **Primary Issue: Swift ABI Incompatibility**
Binary Swift frameworks require **Swift Module Stability** to ensure ABI compatibility across different compilation contexts. Without it:
- Protocol witness tables become context-specific
- Swift symbol signatures are incompatible between build environments
- Cross-framework protocol conformances fail at runtime

### **Secondary Issue: Dependency Source Mismatch**
AgentforceSDK expected dependencies from **SalesforceMobileInterfaces-iOS** while React Native used **SalesforceMobileSDK-iOS**, creating incompatible protocol implementations.

### **Tertiary Issue: Incomplete Dependency Chain**
Swift module stability must be enabled for **ALL** Swift frameworks in the dependency tree, not just the primary frameworks.

## **Complete Solution Applied**

### **1. Dependency Source Alignment**
Updated both projects to use **SalesforceMobileInterfaces-iOS** as the common source:

**AgentforceSDK/Podfile & ReactNative/Podfile:**
```ruby
pod 'SalesforceLogging', :git => 'https://github.com/forcedotcom/SalesforceMobileInterfaces-iOS.git', :tag => '1.0.0'
pod 'SalesforceNavigation', :git => 'https://github.com/forcedotcom/SalesforceMobileInterfaces-iOS.git', :tag => '1.0.0'
pod 'SalesforceNetwork', :git => 'https://github.com/forcedotcom/SalesforceMobileInterfaces-iOS.git', :tag => '1.0.0'
pod 'SalesforceUser', :git => 'https://github.com/forcedotcom/SalesforceMobileInterfaces-iOS.git', :tag => '1.0.0'
```

### **2. Universal Swift Module Stability**
Enabled `BUILD_LIBRARY_FOR_DISTRIBUTION=YES` for **ALL** Swift frameworks in the dependency chain:

**Complete Framework Matrix:**
```ruby
post_install do |installer|
  installer.pods_project.targets.each do |target|
    if target.name == 'SalesforceLogging' || target.name == 'SalesforceNavigation' ||
       target.name == 'SalesforceNetwork' || target.name == 'SalesforceUser' ||
       target.name == 'LiveKitClient' || target.name == 'DequeModule' ||
       target.name == 'SwiftProtobuf' || target.name == 'OrderedCollections'
      target.build_configurations.each do |config|
        config.build_settings['BUILD_LIBRARY_FOR_DISTRIBUTION'] = 'YES'
      end
    end
  end
end
```

### **3. Framework Build Configuration**
Updated build scripts to use `BUILD_LIBRARY_FOR_DISTRIBUTION=YES` for AgentforceSDK and AgentforceService XCFrameworks.

## **Key Technical Insights**

### **Swift Module Stability Rule**
> **Every Swift framework in the entire dependency tree must be built with `BUILD_LIBRARY_FOR_DISTRIBUTION=YES` to enable binary distribution**

### **Dependency Chain Analysis**
**AgentforceSDK Binary** requires:
- ✅ AgentforceService (with module stability)
  - ✅ LiveKitClient (with module stability)
    - ✅ DequeModule (with module stability)
    - ✅ SwiftProtobuf (with module stability)
    - ✅ OrderedCollections (with module stability)
  - ✅ SalesforceLogging (with module stability)
  - ✅ SalesforceNetwork (with module stability)
- ✅ SalesforceNavigation (with module stability)
- ✅ SalesforceUser (with module stability)

### **Protocol Witness Table Compatibility**
Swift protocols generate witness tables that must be ABI-compatible across frameworks. Different dependency sources or missing module stability breaks this compatibility.

## **Resolution Timeline**

1. **SalesforceLogging Error** → Fixed by aligning dependency sources to SalesforceMobileInterfaces-iOS
2. **SalesforceNavigation Error** → Fixed by ensuring AgentforceSDK also used aligned dependencies
3. **LiveKitClient Error** → Fixed by adding `BUILD_LIBRARY_FOR_DISTRIBUTION=YES` to LiveKitClient
4. **DequeModule/SwiftProtobuf Errors** → Fixed by adding module stability to LiveKitClient dependencies
5. **OrderedCollections Error** → Fixed by completing the entire dependency chain with module stability

## **Final Architecture**

```
React Native App (Binary Distribution)
├── AgentforceSDK.xcframework ✅ Module Stable
├── AgentforceService.xcframework ✅ Module Stable
├── SalesforceMobileInterfaces-iOS Frameworks:
│   ├── SalesforceLogging 1.0.0 ✅ Module Stable
│   ├── SalesforceNavigation 1.0.0 ✅ Module Stable
│   ├── SalesforceNetwork 1.0.0 ✅ Module Stable
│   └── SalesforceUser 1.0.0 ✅ Module Stable
└── LiveKitClient Dependencies:
    ├── LiveKitClient 2.7.2 ✅ Module Stable
    ├── DequeModule 1.1.4 ✅ Module Stable
    ├── SwiftProtobuf 1.31.0 ✅ Module Stable
    └── OrderedCollections 1.1.4 ✅ Module Stable
```

## **Universal Rules for Binary Swift Framework Distribution**

1. **Source Consistency**: All frameworks must use identical dependency sources
2. **Complete Module Stability**: Every Swift framework in the dependency chain needs `BUILD_LIBRARY_FOR_DISTRIBUTION=YES`
3. **ABI Compatibility**: Protocol witness tables must be generated with stable Swift interfaces
4. **Version Alignment**: All transitive dependencies must use compatible versions

## **Result: Complete Success** ✅

- **Binary XCFramework Distribution**: Fully functional
- **All Symbol Errors**: Resolved
- **React Native Integration**: Successfully running with binary AgentforceSDK
- **Build Performance**: Improved (binary consumption vs source compilation)
- **Maintainability**: Enhanced (framework versioning and distribution)

**Binary Swift framework distribution is achievable with proper dependency management and universal module stability configuration.**