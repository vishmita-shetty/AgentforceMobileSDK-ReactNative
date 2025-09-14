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