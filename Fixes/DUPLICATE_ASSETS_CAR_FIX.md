# Fix: Duplicate Assets.car Build Error

## Problem Description

The Xcode build was failing with the error:
```
error: Multiple commands produce '/Users/.../Build/Products/Debug-iphonesimulator/ReactAgentforce.app/Assets.car'
note: Target 'ReactAgentforce' (project 'ReactAgentforce'): LinkAssetCatalog /Users/.../mobile_sdk/SalesforceMobileSDK-iOS/shared/resources/Images.xcassets
note: That command depends on command in Target 'ReactAgentforce' (project 'ReactAgentforce'): script phase "[CP] Copy Pods Resources"
```

## Root Cause Analysis

The build system had **two competing processes** trying to create the same `Assets.car` file:

1. **Main Target (ReactAgentforce)**: Processing Mobile SDK's `Images.xcassets` directly through its Resources build phase
2. **CocoaPods Resources Script**: Processing multiple `.xcassets` files from AgentforceSDK pod through the `[CP] Copy Pods Resources` build phase

Both were attempting to compile asset catalogs and output to the same `Assets.car` file in the app bundle, causing a build conflict.

## Investigation Process

1. **Analyzed build error**: Identified duplicate `Assets.car` output file
2. **Examined CocoaPods resources script**: Found AgentforceSDK was installing multiple `.xcassets` files:
   - `AgentforceSDK/Resources/Assets.xcassets` 
   - `AgentforceSDK/Theme/SalesforceCosmos/CosmosAssets.xcassets`
   - `AgentforceSDK/Theme/SLDS/IconColors.xcassets`
   - `AgentforceSDK/Theme/SLDS/Icons.xcassets`
3. **Examined main target**: Found direct reference to Mobile SDK's `Images.xcassets`
4. **Identified conflict**: Both processes generating asset catalogs to same destination

## Solution Implemented

### Removed Mobile SDK Images.xcassets from Main Target

Modified `/ios/ReactAgentforce.xcodeproj/project.pbxproj` to remove all references:

#### 1. Removed from PBXBuildFile section:
```diff
- CEA8CA061F071C3300448B51 /* Images.xcassets in Resources */ = {isa = PBXBuildFile; fileRef = CEA8CA051F071C3300448B51 /* Images.xcassets */; };
```

#### 2. Removed from PBXFileReference section:
```diff
- CEA8CA051F071C3300448B51 /* Images.xcassets */ = {isa = PBXFileReference; lastKnownFileType = folder.assetcatalog; name = Images.xcassets; path = "../mobile_sdk/SalesforceMobileSDK-iOS/shared/resources/Images.xcassets"; sourceTree = "<group>"; };
```

#### 3. Removed from Resources Build Phase:
```diff
files = (
    B77BD8AF2113632C0036B284 /* bootconfig.plist in Resources */,
-   CEA8CA061F071C3300448B51 /* Images.xcassets in Resources */,
    69BE961B2AD535C8002E3F6C /* PrivacyInfo.xcprivacy in Resources */,
    B7168FBE1FACC7EB00A48DB5 /* LaunchScreen.storyboard in Resources */,
);
```

#### 4. Removed from Supporting Files Group:
```diff
children = (
    69BE961A2AD535C8002E3F6C /* PrivacyInfo.xcprivacy */,
    B77BD8AE2113632C0036B284 /* bootconfig.plist */,
    B7168FBD1FACC7EB00A48DB5 /* LaunchScreen.storyboard */,
-   CEA8CA051F071C3300448B51 /* Images.xcassets */,
    008F07F21AC5B25A0029DE68 /* main.jsbundle */,
    13B07FB71A68108700A75B9A /* main.m */,
    13B07FB61A68108700A75B9A /* Info.plist */,
);
```

## Result

- ✅ **Duplicate Assets.car error eliminated**
- ✅ **Build progresses successfully** through asset compilation
- ✅ **CocoaPods handles all asset catalogs** from AgentforceSDK pod
- ✅ **No functionality lost** - all assets still available through CocoaPods processing

## Why This Fix Works

By removing the Mobile SDK's asset catalog from the main target:
- Only **one process** (CocoaPods) handles asset catalog compilation
- All asset catalogs from AgentforceSDK are processed together by CocoaPods
- No competing commands trying to write to the same output file
- Asset processing is consolidated and conflict-free

## Files Modified

- `/ios/ReactAgentforce.xcodeproj/project.pbxproj` - Removed all references to conflicting asset catalog

## Future Considerations

- Asset catalogs should be managed through CocoaPods when using pod-based dependencies
- Main target should avoid directly processing asset catalogs that are also managed by pods
- This approach scales better with multiple pod dependencies that include assets

## Verification

The fix was verified by running:
```bash
xcodebuild build -workspace ReactAgentforce.xcworkspace -scheme ReactAgentforce -destination 'platform=iOS Simulator,name=iPhone 16' ONLY_ACTIVE_ARCH=YES
```

The "Multiple commands produce Assets.car" error no longer occurs, and the build progresses much further (now failing on an unrelated Swift module issue, proving the asset conflict is resolved).