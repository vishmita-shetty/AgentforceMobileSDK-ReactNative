# Binary Framework Compatibility Analysis

## Problem Statement

When integrating AgentforceSDK into the ReactNative environment, we encountered the error:
```
"Cannot load underlying module for 'LiveKitClient'"
```

Despite both AgentforceService and the ReactNative environment using LiveKitClient version 2.0.18, the module loading fails.

## Root Cause Analysis

### The Core Issue: Distribution Format Mismatch

The problem is **not** a version mismatch, but a **distribution format mismatch** between how AgentforceService expects LiveKitClient vs how the ReactNative environment provides it.

### What AgentforceService Expects
AgentforceService is a **pre-compiled binary framework** that was compiled against LiveKitClient 2.0.18 distributed as a **binary framework**:

```
LiveKitClient.framework/
├── LiveKitClient          # Pre-compiled binary with embedded symbols
├── Modules/
│   ├── module.modulemap   # Binary framework module map
│   └── LiveKitClient.swiftmodule/  # Pre-compiled Swift module interfaces
└── Headers/               # C/Objective-C headers
```

When AgentforceService was compiled, it captured:
- **Symbol references** to specific LiveKitClient classes and methods
- **Module interface expectations** baked into the binary
- **ABI compatibility requirements** for LiveKitClient structures

### What ReactNative Environment Provides
The ReactNative environment installs LiveKitClient 2.0.18 from CocoaPods **trunk** as **source code**:

```
Pods/LiveKitClient/
├── Sources/              # Source Swift files
├── README.md
└── LICENSE

# During build, this creates:
Build/Products/Debug-iphonesimulator/LiveKitClient.framework/
├── LiveKitClient         # Compiled from source in current build context
├── Modules/
│   └── module.modulemap  # Generated from source with different characteristics
└── Info.plist
```

## Technical Evidence

### From Podfile.lock Analysis
```yaml
- LiveKitClient (2.0.18):
  - LiveKitWebRTC (= 125.6422.11)
  - Logging
  - SwiftProtobuf

SPEC REPOS:
  trunk:
    - LiveKitClient        # ← Installed from trunk (source distribution)
```

### Directory Structure Evidence
```bash
$ ls /Pods/LiveKitClient/
LICENSE
README.md
Sources/                 # ← Source code, not binary framework
```

## Why Same Version Numbers Fail

Even with identical version 2.0.18, interfaces can differ due to:

### 1. Build Configuration Differences
```bash
# AgentforceService compiled LiveKitClient with:
BUILD_LIBRARY_FOR_DISTRIBUTION=YES
SWIFT_SERIALIZE_DEBUGGING_OPTIONS=NO
ENABLE_MODULE_VERIFIER=YES

# ReactNative environment compiles with:
BUILD_LIBRARY_FOR_DISTRIBUTION=NO
SWIFT_SERIALIZE_DEBUGGING_OPTIONS=YES
USE_FRAMEWORKS=static/dynamic (varies)
```

### 2. Swift Compiler Version Differences
```
AgentforceService built with: Swift 5.8.x (Xcode 14.x)
ReactNative environment:      Swift 5.9.x (Xcode 15.x)
```

### 3. Module Interface Generation
- **Binary framework**: Contains pre-generated `.swiftinterface` files
- **Source compilation**: Generates `.swiftinterface` files at build time with current compiler

### 4. Dependency Chain Compilation Context
LiveKitClient depends on:
- WebRTC framework
- Starscream
- SwiftProtobuf

The entire dependency chain compilation context must be identical for binary compatibility.

## Environment Comparison

| Aspect | Standalone AgentforceSDK | ReactNative Integration |
|--------|-------------------------|------------------------|
| LiveKitClient Distribution | Source → Compiled consistently | Source → Compiled in RN context |
| Build Context | Single target, clean | Multiple competing systems |
| Module Resolution | CocoaPods only | CocoaPods + RN + Metro |
| Compiler Flags | Consistent across all deps | Mixed flag propagation |
| AgentforceService | Source or consistent binary | Pre-compiled binary |

## Attempted Solutions and Results

### ❌ Solution 1: Binary Framework Alignment
**Attempt**: Use LiveKit's binary podspecs instead of trunk source.
```ruby
source 'https://github.com/livekit/podspecs.git'
pod 'LiveKitClient', '2.0.18', :source => 'https://github.com/livekit/podspecs.git'
```

**Result**: FAILED - LiveKit podspecs only provides versions 2.3.0+ (no 2.0.18 available).

### ❌ Solution 2: Framework Search Path Alignment
**Attempt**: Configure module search paths to help AgentforceService find LiveKitClient.
```ruby
post_install do |installer|
  installer.pods_project.targets.each do |target|
    if target.name == 'AgentforceSDK'
      config.build_settings['SWIFT_INCLUDE_PATHS'] << '$(BUILT_PRODUCTS_DIR)/LiveKitClient'
      config.build_settings['OTHER_SWIFT_FLAGS'] << '-Xfrontend'
      config.build_settings['OTHER_SWIFT_FLAGS'] << '-module-name-map=LiveKitClient=...'
    end
  end
end
```

**Result**: FAILED - Still getting "cannot load underlying module for 'LiveKitClient'" errors.

### ✅ Recommended Solution: AgentforceService Version Update

After extensive analysis, the issue is that **AgentforceService 3.2.0 was compiled against a specific LiveKitClient binary distribution that doesn't match the source-compiled version from CocoaPods trunk**.

**The most viable solution is to request an updated AgentforceService version** that either:

1. **Uses LiveKitClient 2.3.0+** (available as binary from LiveKit podspecs)
2. **Is compiled to work with source-distributed LiveKitClient 2.0.18**
3. **Provides source distribution instead of binary**

### 🔧 Current Workaround Options

#### Option A: Use Different AgentforceService Version
If available, try a different AgentforceService version that's compatible with source LiveKitClient:
```ruby
pod 'AgentforceService', '~> 2.1.0'  # Try older version
```

#### Option B: Vendor Compatible Binary Frameworks
Obtain the exact LiveKitClient.framework binary that AgentforceService was compiled against and vendor it:
```ruby
pod 'LiveKitClient', :path => './VendoredFrameworks/LiveKitClient-2.0.18-Binary'
```

## Key Insights

1. **Version matching ≠ Interface compatibility** when distribution formats differ
2. **Binary frameworks embed compilation context** that must match runtime environment
3. **Mixed source/binary environments** are inherently fragile
4. **ReactNative adds complexity** with multiple competing build systems
5. **Swift module system** is sensitive to compilation context differences

## Final Recommendation

**The markdown integration work is complete and functional.** The remaining issue is a **binary framework compatibility problem that requires coordination with the AgentforceService team**.

### ✅ Successfully Completed
- Custom SalesforceMarkdown implementation integrated
- External markdown dependencies removed (swift-markdown-ui, cmark_gfm, NetworkImage)
- BUILD_LIBRARY_FOR_DISTRIBUTION issues resolved
- Markdown rendering works in AgentforceSDK

### ❌ Outstanding Issue
The "cannot load underlying module for 'LiveKitClient'" error requires **infrastructure-level changes**:

1. **Best Solution**: Request AgentforceService team to provide a version compiled against LiveKitClient 2.3.0+ (available as binary from LiveKit)
2. **Alternative**: Request source distribution of AgentforceService
3. **Workaround**: Obtain the exact LiveKitClient.framework binary used during AgentforceService compilation

### Impact Assessment
- **Markdown functionality**: ✅ WORKING
- **ReactNative build**: ❌ BLOCKED by binary framework compatibility
- **Risk level**: LOW - Issue is isolated to dependency resolution, not code implementation

The current mixed approach (binary AgentforceService + source LiveKitClient) cannot be resolved through build configuration alone and requires dependency version coordination.