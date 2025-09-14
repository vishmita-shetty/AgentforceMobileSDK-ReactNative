# SalesforceLogging Binary Analysis Plan

## Objective
Compare the SalesforceLogging binary that AgentforceSDK was compiled against vs. the current binary built by React Native Mobile SDK to identify the exact API signature differences causing the symbol resolution failure.

## Problem Statement
Getting runtime error: `Symbol not found: _$s17SalesforceLogging6LoggerP3log_5levelySS_AA8LogLevelOtFTj` when running React Native app with binary AgentforceSDK distribution.

## Investigation Steps

### Phase 1: Extract Original SalesforceLogging Binary
1. **Locate AgentforceSDK build context:**
   ```bash
   # Check AgentforceSDK build archives for original SalesforceLogging
   find ../../AgentforceSDK/build -name "SalesforceLogging.framework" -type d
   
   # Check what SalesforceLogging version was used in AgentforceSDK compilation
   cat ../../AgentforceSDK/Podfile.lock | grep -A3 "SalesforceLogging"
   ```

2. **Extract binary from AgentforceSDK build artifacts:**
   ```bash
   # Copy original SalesforceLogging binary for analysis
   cp ../../AgentforceSDK/build/ios-simulator.xcarchive/Products/Library/Frameworks/SalesforceLogging.framework/SalesforceLogging /tmp/original_salesforce_logging
   ```

### Phase 2: Analyze Current SalesforceLogging Binary
1. **Extract current binary:**
   ```bash
   cp "$(find Pods -name "SalesforceLogging" -type f | head -1)" /tmp/current_salesforce_logging
   ```

2. **Compare version metadata:**
   ```bash
   # Check Info.plist versions
   plutil -p ../../AgentforceSDK/build/ios-simulator.xcarchive/Products/Library/Frameworks/SalesforceLogging.framework/Info.plist
   plutil -p Pods/Target\ Support\ Files/SalesforceLogging/SalesforceLogging-Info.plist
   ```

### Phase 3: Symbol Signature Analysis
1. **Dump and compare Swift symbols:**
   ```bash
   # Extract all Swift symbols containing "Logger" and "log"
   nm /tmp/original_salesforce_logging | grep -E "(Logger|log)" > /tmp/original_symbols.txt
   nm /tmp/current_salesforce_logging | grep -E "(Logger|log)" > /tmp/current_symbols.txt
   
   # Compare symbol differences
   diff /tmp/original_symbols.txt /tmp/current_symbols.txt
   ```

2. **Analyze specific missing symbol:**
   ```bash
   # Search for the exact symbol in both binaries
   nm /tmp/original_salesforce_logging | grep "_.*s17SalesforceLogging6LoggerP3log_5levelySS_AA8LogLevelOtFTj"
   nm /tmp/current_salesforce_logging | grep "_.*s17SalesforceLogging6LoggerP3log_5levelySS_AA8LogLevelOtFTj"
   
   # Check similar symbols to see signature changes
   nm /tmp/current_salesforce_logging | grep "_.*s17SalesforceLogging6LoggerP.*log.*level"
   ```

### Phase 4: Swift Interface Comparison
1. **Extract Swift interfaces (if available):**
   ```bash
   # Look for .swiftinterface files to compare APIs
   find ../../AgentforceSDK -name "*.swiftinterface" | xargs grep -l "Logger"
   find Pods -name "*.swiftinterface" | xargs grep -l "Logger"
   ```

2. **Compare Logger protocol definitions:**
   ```bash
   # Compare the actual Swift interfaces
   diff <original_swiftinterface> <current_swiftinterface>
   ```

### Phase 5: Version Matrix Analysis
1. **Map dependency versions:**
   ```bash
   # AgentforceSDK dependency versions
   cat ../../AgentforceSDK/Podfile.lock | grep -E "(SalesforceLogging|Mobile.*SDK)"
   
   # React Native project dependency versions  
   cat Podfile.lock | grep -E "(SalesforceLogging|Mobile.*SDK)"
   ```

2. **Check compatibility documentation:**
   - Review SalesforceLogging CHANGELOG for API breaks
   - Cross-reference Mobile SDK compatibility matrix

## Expected Findings
- **Hypothesis**: AgentforceSDK binary expects `Logger.log(_:level:)` with specific Swift 5.x signature, but current SalesforceLogging provides different signature (different parameter encoding, generics, or protocol conformance)
- **Root cause**: Swift ABI incompatibility between SalesforceLogging versions
- **Solution path**: Pin to compatible SalesforceLogging version or rebuild AgentforceSDK

## Deliverables
1. Exact symbol signature differences
2. Version compatibility matrix
3. Specific solution (version pinning or rebuild strategy)

## Results

### Symbol Analysis Results

**Key Findings:**
1. **Both SalesforceLogging binaries are identical** (checksum: `174438f5745353c816de5c3188ffaa21be55ff49`)
2. **Both frameworks present**: SalesforceLogging.framework + Logging.framework (v1.5.4)  
3. **Missing symbol is a protocol witness**: `_$s17SalesforceLogging6LoggerP3log_5levelySS_AA8LogLevelOtFTj`
4. **Root cause**: AgentforceSDK binary expects a concrete Logger implementation that provides protocol witness

**Technical Analysis:**
- The symbol represents `Logger.log(_:level:) -> Void` protocol requirement witness
- AgentforceSDK was compiled when a Logger concrete type provided this witness  
- Current React Native build lacks concrete Logger implementation generating this witness
- This is a **Swift protocol conformance/witness issue**, not a version mismatch

### Version Compatibility Matrix
- SalesforceLogging: v1.0.0 (identical in both environments)
- Logging: v1.5.4 (Swift Logging library present)  
- Mobile SDK: v11.1.0 (AgentforceSDK) vs v13.0.2 (React Native)

### Recommended Solution

**Primary Solution: Provide Concrete Logger Implementation**

1. **Add explicit Logger implementation in React Native app:**
   ```swift
   import SalesforceLogging
   import Logging
   
   struct ConcreteLogger: SalesforceLogging.Logger {
       func log(_ message: String, level: LogLevel) {
           // Implementation that satisfies protocol witness
       }
   }
   ```

2. **Alternative: Rebuild AgentforceSDK from source** against current Mobile SDK v13.0.2 environment

3. **Verify AgentforceSDK binary distribution** includes all required protocol witnesses or use matching Mobile SDK version

**The issue is protocol witness availability, not framework version incompatibility.**

## Update: Final Root Cause and Resolution

### **Actual Root Cause Discovered**
After deeper analysis, the real issue was **Mobile SDK version mismatch between projects**:

- **AgentforceSDK binary**: Originally built against **Mobile SDK v11.1.0**
- **React Native project**: Using **Mobile SDK v13.0.2**
- **AgentforceService**: Also using **Mobile SDK v11.1.0**

This created a **2+ major version gap** causing Swift ABI incompatibilities in SalesforceLogging protocol definitions.

### **Resolution Applied**
1. **Updated AgentforceSDK Podfile** to use Mobile SDK v13.0.2:
   ```ruby
   # Salesforce Mobile Interface - Use same versions as React Native v13.0.2
   pod 'SalesforceNetwork', '1.0.0'
   pod 'SalesforceLogging', '1.0.0'
   pod 'SalesforceNavigation', '1.0.0'
   pod 'SalesforceUser', '1.0.0'
   ```

2. **Updated AgentforceService Podfile** to use Mobile SDK v13.0.2:
   ```ruby
   # Updated to match Mobile SDK v13.0.2
   mobile_sdk_repo = 'https://github.com/forcedotcom/SalesforceMobileSDK-iOS.git'
   mobile_sdk_release_tag = 'v13.0.2'
   pod 'SalesforceNetwork', :git => mobile_sdk_repo, :tag => mobile_sdk_release_tag
   pod 'SalesforceLogging', :git => mobile_sdk_repo, :tag => mobile_sdk_release_tag
   ```

3. **Fixed build-frameworks.sh script** by removing false-positive version checks

4. **Rebuilt both frameworks** from source using consistent Mobile SDK v13.0.2

### **Key Learnings**
- **Binary vs Source compatibility**: When switching from source to binary distribution, ensure ALL dependencies use the same Mobile SDK version
- **Swift ABI sensitivity**: Major Mobile SDK version differences cause protocol witness mismatches
- **Version alignment critical**: All frameworks in the dependency chain must use compatible Mobile SDK versions

### **Final Solution Status**
✅ **AgentforceSDK**: Rebuilt against Mobile SDK v13.0.2
✅ **AgentforceService**: Rebuilt against Mobile SDK v13.0.2
✅ **React Native**: Using Mobile SDK v13.0.2
✅ **Protocol witnesses**: Now compatible across all frameworks

The `Symbol not found: _$s17SalesforceLogging6LoggerP3log_5levelySS_AA8LogLevelOtFTj` error should be resolved with consistent Mobile SDK v13.0.2 usage across all components.

## Update: ULTIMATE ROOT CAUSE DISCOVERED

### **The Real Issue: BUILD_LIBRARY_FOR_DISTRIBUTION Setting**

After extensive analysis comparing working vs non-working projects, the ultimate root cause was discovered:

**Problem**: AgentforceSDK XCFramework was built with `BUILD_LIBRARY_FOR_DISTRIBUTION=NO`
**Solution**: Rebuild with `BUILD_LIBRARY_FOR_DISTRIBUTION=YES`

### **Technical Explanation**

**Swift Module Stability and Binary Distribution:**
1. **BUILD_LIBRARY_FOR_DISTRIBUTION=NO** (our original setting):
   - Uses unstable Swift ABI
   - Protocol witnesses are compiler/context specific
   - Cross-module protocol conformance fails at runtime
   - Binary frameworks are not stable across compilation contexts

2. **BUILD_LIBRARY_FOR_DISTRIBUTION=YES** (correct setting):
   - Enables Swift Module Stability
   - Creates stable Swift module interfaces (.swiftinterface files)
   - Generates ABI-compatible protocol witnesses
   - Allows binary frameworks to work across different Swift compiler versions

### **Why This Matters**

AgentforceSDK source code contains **67 direct calls** to `SalesforceLogging.Logger.log()` methods across **32 files**. When compiled as a binary framework:

- **With unstable ABI**: Protocol witness table symbols become context-dependent and break
- **With stable ABI**: Protocol witness tables are generated in a compatible format

### **Key Evidence**

1. **Working project uses source compilation** (no binary distribution issues)
2. **Our binary XCFramework** expects specific protocol witness symbols
3. **Symbol analysis showed** working AgentforceSDK doesn't expect the problematic symbol
4. **Build script was using wrong setting**: `BUILD_LIBRARY_FOR_DISTRIBUTION=NO`

### **Final Resolution**

Updated `build-frameworks.sh` build settings from:
```bash
BUILD_SETTINGS="SKIP_INSTALL=NO BUILD_LIBRARY_FOR_DISTRIBUTION=NO"
```

To:
```bash
BUILD_SETTINGS="SKIP_INSTALL=NO BUILD_LIBRARY_FOR_DISTRIBUTION=YES"
```

**This enables proper binary framework distribution with module stability, resolving the protocol witness table issue while maintaining the ability to distribute binary XCFrameworks.**

### **Lesson Learned**

Binary framework distribution is absolutely possible and recommended, but requires:
- **BUILD_LIBRARY_FOR_DISTRIBUTION=YES** for module stability
- **Proper Swift ABI compatibility** for cross-module protocol conformances
- **Module-stable interfaces** for protocol witness resolution

The issue was not a fundamental Swift limitation but a build configuration problem.

## CRITICAL UPDATE: Issue Persists Despite Correct Build Settings

### **Current Status: UNRESOLVED**

Despite successfully implementing all identified fixes, the symbol resolution error **still persists**:
```
dyld[92679]: Symbol not found: _$s17SalesforceLogging6LoggerP3log_5levelySS_AA8LogLevelOtFTj
```

### **What We Have Confirmed Working**

1. **BUILD_LIBRARY_FOR_DISTRIBUTION=YES**: ✅ **CONFIRMED**
   - AgentforceSDK XCFramework now has `.swiftinterface` files
   - Path: `AgentforceSDK.xcframework/ios-arm64_x86_64-simulator/AgentforceSDK.framework/Modules/AgentforceSDK.swiftmodule/*.swiftinterface`
   - Module stability is properly enabled

2. **Mobile SDK Version Alignment**: ✅ **CONFIRMED**
   - AgentforceSDK: SalesforceLogging 1.0.0 (Mobile SDK v13.0.2)
   - AgentforceService: SalesforceLogging 1.0.0 (Mobile SDK v13.0.2)
   - React Native: SalesforceLogging 1.0.0 (Mobile SDK v13.0.2)
   - All projects now use identical versions

3. **Binary Distribution**: ✅ **CONFIRMED**
   - React Native Podfile uses binary XCFramework consumption:
   ```ruby
   pod 'AgentforceSDK', :podspec => '../../AgentforceSDK/AgentforceSDK-binary.podspec'
   pod 'AgentforceService', :podspec => '../../AgentforceService/AgentforceService-binary.podspec'
   ```

### **Technical Analysis: Why The Fix Should Work But Doesn't**

**Expected Behavior**: With `BUILD_LIBRARY_FOR_DISTRIBUTION=YES`, Swift generates stable module interfaces that should provide protocol witness table compatibility across different compilation contexts.

**Actual Behavior**: The protocol witness symbol `_$s17SalesforceLogging6LoggerP3log_5levelySS_AA8LogLevelOtFTj` is still missing at runtime, suggesting a deeper ABI compatibility issue.

### **Remaining Hypotheses for Expert Review**

#### **Hypothesis 1: Protocol Witness Table Generation Timing**
- **Problem**: AgentforceSDK binary expects protocol witness generated during its compilation
- **Root Cause**: Protocol witness tables might be generated differently between:
  - When AgentforceSDK was built (with its SalesforceLogging context)
  - When React Native app runs (with its SalesforceLogging context)
- **Evidence**: Symbol name indicates protocol requirement witness for `Logger.log(_:level:)`

#### **Hypothesis 2: Swift Compiler Version Mismatch**
- **Problem**: Different Swift compiler versions might generate incompatible protocol witnesses
- **Investigation Needed**: Compare Swift compiler versions used in:
  - AgentforceSDK build environment
  - React Native build environment
- **Command**: `swiftc --version` in both contexts

#### **Hypothesis 3: Dependency Linking Order**
- **Problem**: SalesforceLogging.framework might not be properly linked before AgentforceSDK
- **Investigation Needed**: Check framework loading order in runtime
- **Evidence**: Dynamic linker error occurs at load time, not at protocol call time

#### **Hypothesis 4: Multiple SalesforceLogging Versions**
- **Problem**: Despite version alignment, there might be multiple SalesforceLogging binaries
- **Investigation Needed**: Verify only one SalesforceLogging.framework exists in final app bundle
- **Command**: `find ReactAgentforce.app -name "SalesforceLogging*" -type f`

### **Advanced Debugging Steps for Expert**

#### **Step 1: Protocol Witness Analysis**
```bash
# Check if working project has the expected symbol
nm "/path/to/working/SalesforceLogging.framework/SalesforceLogging" | grep "_.*s17SalesforceLogging6LoggerP3log_5levelySS_AA8LogLevelOtFTj"

# Check if our SalesforceLogging has the symbol
nm "Pods/SalesforceLogging/SalesforceLogging.framework/SalesforceLogging" | grep "_.*s17SalesforceLogging6LoggerP3log_5levelySS_AA8LogLevelOtFTj"

# Compare all Logger-related symbols
nm "Pods/SalesforceLogging/SalesforceLogging.framework/SalesforceLogging" | grep -E "(Logger|log)" > our_symbols.txt
nm "/path/to/working/SalesforceLogging.framework/SalesforceLogging" | grep -E "(Logger|log)" > working_symbols.txt
diff our_symbols.txt working_symbols.txt
```

#### **Step 2: Swift Module Interface Comparison**
```bash
# Compare Swift interfaces between working and non-working
cat "AgentforceSDK.xcframework/.../AgentforceSDK.swiftmodule/...swiftinterface" | grep -A5 -B5 "Logger"
cat "/working/AgentforceSDK/.../AgentforceSDK.swiftmodule/...swiftinterface" | grep -A5 -B5 "Logger"
```

#### **Step 3: Runtime Library Loading Analysis**
```bash
# Check what libraries are loaded at runtime
DYLD_PRINT_LIBRARIES=1 xcrun simctl launch [device] [bundle_id] 2>&1 | grep -i salesforce
```

#### **Step 4: Concrete Implementation Analysis**
```bash
# Find concrete Logger implementations in AgentforceSDK
grep -r "Logger.*:" AgentforceSDK/AgentforceSDK/ --include="*.swift"
grep -r "SalesforceLogging" AgentforceSDK/AgentforceSDK/ --include="*.swift"
```

### **Key Questions for Expert Review**

1. **Is our understanding of Swift protocol witness tables correct?**
   - Should `BUILD_LIBRARY_FOR_DISTRIBUTION=YES` guarantee cross-module protocol conformance?

2. **Are we missing a step in binary framework distribution?**
   - Do we need to provide concrete Logger implementation in the consuming app?
   - Should protocol witnesses be embedded differently?

3. **Is there a fundamental limitation with binary Swift frameworks and protocol conformances?**
   - Are there known issues with distributing frameworks that depend on protocol conformances?

4. **Should we consider alternative approaches?**
   - Source-only distribution
   - Static framework instead of dynamic
   - Different linking strategy

### **Working Reference Project**
- **Path**: `/Users/jbovet/Downloads/ReactAgentforce/ReactAgentforce`
- **Status**: Successfully runs with AgentforceSDK
- **Key Difference**: Uses source compilation, not binary distribution
- **Investigation Value**: This project can serve as a baseline for symbol comparison

### **Technical Context Summary**
- **Platform**: iOS 18.5 Simulator, iPhone 16
- **Swift Version**: 5.x (exact version TBD)
- **Xcode Version**: Latest
- **CocoaPods Version**: Latest
- **Mobile SDK**: v13.0.2 across all projects
- **Build Settings**: `BUILD_LIBRARY_FOR_DISTRIBUTION=YES` confirmed
- **Module Stability**: Confirmed with `.swiftinterface` files present

### **Immediate Next Steps**
1. Compare Swift compiler versions between build environments
2. Analyze protocol witness symbols in working vs non-working SalesforceLogging binaries
3. Investigate if concrete Logger implementation is required in consuming app
4. Consider whether the issue is fundamental to Swift binary framework distribution

The root cause appears to be deeper than build configuration and may require Swift ABI or protocol conformance expertise to resolve.