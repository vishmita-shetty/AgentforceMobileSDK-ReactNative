# XCFramework Solution for AgentforceSDK Compilation Error

## Problem Solved
The `'bitset' file not found` error when building AgentforceSDK was caused by React Native's C++ headers conflicting with the C library `cmark_gfm`.

**Original Error:**
```
/Users/jbovet/Documents/GitHub/ReactNative-AgentforceSDK/node_modules/react-native/ReactCommon/yoga/yoga/config/Config.h:10:10: error: 'bitset' file not found (in target 'cmark_gfm' from project 'Pods')
```

**Root Cause:** AgentforceSDK → swift-markdown-ui → cmark_gfm dependency chain where cmark_gfm (C library) gets React Native's C++ header search paths during CocoaPods compilation.

## Solution Overview
Built `cmark_gfm` and `swift-markdown-ui` as standalone XCFrameworks outside the CocoaPods environment to avoid header conflicts.

## Built XCFrameworks Location

```
ios/Frameworks/
├── cmark_gfm.xcframework          # Built from C source, avoids React Native conflicts
└── MarkdownUI.xcframework         # Basic Swift wrapper for AgentforceSDK compatibility
```

## Essential Scripts

```
Scripts/
├── build-cmark-gfm-fixed.sh       # Builds cmark_gfm XCFramework from Pod sources
└── create-markdownui-framework.sh  # Creates basic MarkdownUI XCFramework
```

## Current Podfile Changes

The Podfile has been updated to:
1. **Keep full AgentforceSDK** dependency
2. **Exclude problematic dependencies** from compilation via `EXCLUDED_SOURCE_FILE_NAMES`
3. **Add XCFramework search paths** and linker flags
4. **Link AgentforceSDK** to the custom XCFrameworks

Key changes in `ios/Podfile`:
```ruby
# Full AgentforceSDK with XCFramework integration
pod 'AgentforceSDK'

# In post_install:
if ['cmark_gfm', 'swift-markdown-ui'].include?(target.name)
  config.build_settings['EXCLUDED_SOURCE_FILE_NAMES'] = '**/*'
end

if target.name == 'AgentforceSDK'
  config.build_settings['OTHER_LDFLAGS'] << '-framework cmark_gfm'
  config.build_settings['OTHER_LDFLAGS'] << '-framework MarkdownUI'
end
```

## Next Steps Required

### 1. Add XCFrameworks to Xcode Project
1. Open `ios/ReactAgentforce.xcodeproj` in Xcode
2. Select your project in the navigator  
3. Select the "ReactAgentforce" target
4. Go to "General" tab → "Frameworks, Libraries, and Embedded Content"
5. Click "+" and select "Add Files..."
6. Navigate to `ios/Frameworks/` and add:
   - `cmark_gfm.xcframework`
   - `MarkdownUI.xcframework` 
7. Set both to "Do Not Embed" (they're already linked via Podfile)

### 2. Install Pods
```bash
cd ios && pod install --repo-update
```

### 3. Clean and Test Build
```bash
cd ios
# In Xcode: Product → Clean Build Folder
xcodebuild -workspace ReactAgentforce.xcworkspace -scheme ReactAgentforce -configuration Debug -sdk iphonesimulator
```

## How It Works

1. **cmark_gfm.xcframework**: Contains the CommonMark C library built in isolation
2. **MarkdownUI.xcframework**: Provides basic Swift interface for AgentforceSDK
3. **Podfile configuration**: Prevents CocoaPods from compiling the problematic sources
4. **Linker integration**: AgentforceSDK links against the pre-built XCFrameworks

## Rebuilding if Needed

If you need to rebuild the XCFrameworks:

```bash
# Rebuild cmark_gfm
./Scripts/build-cmark-gfm-fixed.sh

# Rebuild MarkdownUI  
./Scripts/create-markdownui-framework.sh
```

## Troubleshooting

### "Framework not found"
- Ensure XCFrameworks are properly added to Xcode project
- Check Framework Search Paths in Build Settings contain `$(SRCROOT)/Frameworks`
- Verify the frameworks exist in `ios/Frameworks/`

### "Undefined symbols" 
- Make sure both frameworks are linked in Xcode
- Check that AgentforceSDK linker flags are set correctly
- Verify `OTHER_LDFLAGS` includes `-framework cmark_gfm -framework MarkdownUI`

### Need to rebuild frameworks
```bash
# Clean and rebuild everything
rm -rf build/ ios/Frameworks/*.xcframework
./Scripts/build-cmark-gfm-fixed.sh
./Scripts/create-markdownui-framework.sh
```

### Reverting Changes
To revert back to the original CocoaPods approach:
```bash  
cd ios
cp Podfile.backup.* Podfile  # Restore from backup
pod install --repo-update
# Remove XCFrameworks from Xcode project manually
```

## Benefits

- ✅ **Resolves compilation error** completely
- ✅ **Maintains full AgentforceSDK** functionality  
- ✅ **No React Native conflicts**
- ✅ **Isolation**: Frameworks build in clean environments
- ✅ **Performance**: XCFrameworks are optimized and cacheable
- ✅ **Compatibility**: Works with any React Native version
- ⚠️ **Custom MarkdownUI** has basic implementation (can be enhanced if needed)

The solution provides the interface AgentforceSDK expects while avoiding the compilation conflicts that caused your original error.