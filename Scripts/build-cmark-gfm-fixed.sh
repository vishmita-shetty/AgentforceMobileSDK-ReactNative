#!/bin/bash

# Fixed build script for cmark_gfm using direct compilation with proper universal binary creation
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
IOS_DIR="$PROJECT_ROOT/ios"
BUILD_DIR="$PROJECT_ROOT/build/cmark_gfm_fixed"
XCFRAMEWORK_OUTPUT="$PROJECT_ROOT/ios/Frameworks/cmark_gfm.xcframework"

echo "🔨 Building cmark_gfm XCFramework (fixed approach)..."

# Clean previous builds
rm -rf "$BUILD_DIR"
rm -rf "$XCFRAMEWORK_OUTPUT"
mkdir -p "$BUILD_DIR"

# Check if CocoaPods installation exists
if [ ! -d "$IOS_DIR/Pods/cmark_gfm" ]; then
    echo "❌ Error: cmark_gfm pod not found. Please run 'pod install' first."
    exit 1
fi

CMARK_POD_DIR="$IOS_DIR/Pods/cmark_gfm"
CMARK_SOURCES="$CMARK_POD_DIR/Sources/cmark-gfm"
CMARK_INCLUDES="$CMARK_SOURCES/include"

echo "📁 Using sources from: $CMARK_SOURCES"

# Collect all C source files
C_FILES=$(find "$CMARK_SOURCES" -name "*.c" -not -path "*/test/*" -not -path "*/build/*")
echo "📋 Found $(echo "$C_FILES" | wc -l | xargs) C source files"

# Build for iOS Device (arm64)
echo "📱 Building for iOS Device (arm64)..."
IOS_DEVICE_DIR="$BUILD_DIR/ios-device"
mkdir -p "$IOS_DEVICE_DIR"

/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/clang \
    -arch arm64 \
    -isysroot /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk \
    -miphoneos-version-min=12.0 \
    -I"$CMARK_INCLUDES" \
    -DCMARK_GFM_STATIC_DEFINE=1 \
    -DCMARK_GFM_EXTENSIONS_STATIC_DEFINE=1 \
    -std=c99 \
    -O2 \
    -c \
    $C_FILES \
    -working-directory "$IOS_DEVICE_DIR"

# Create static library for iOS Device
ar rcs "$IOS_DEVICE_DIR/libcmark_gfm.a" "$IOS_DEVICE_DIR"/*.o
ranlib "$IOS_DEVICE_DIR/libcmark_gfm.a"

echo "✅ iOS Device library created"

# Build for iOS Simulator x86_64
echo "📱 Building for iOS Simulator (x86_64)..."
IOS_SIM_X86_DIR="$BUILD_DIR/ios-simulator-x86_64"
mkdir -p "$IOS_SIM_X86_DIR"

/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/clang \
    -arch x86_64 \
    -isysroot /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk \
    -mios-simulator-version-min=12.0 \
    -I"$CMARK_INCLUDES" \
    -DCMARK_GFM_STATIC_DEFINE=1 \
    -DCMARK_GFM_EXTENSIONS_STATIC_DEFINE=1 \
    -std=c99 \
    -O2 \
    -c \
    $C_FILES \
    -working-directory "$IOS_SIM_X86_DIR"

ar rcs "$IOS_SIM_X86_DIR/libcmark_gfm.a" "$IOS_SIM_X86_DIR"/*.o
ranlib "$IOS_SIM_X86_DIR/libcmark_gfm.a"

echo "✅ iOS Simulator x86_64 library created"

# Build for iOS Simulator arm64
echo "📱 Building for iOS Simulator (arm64)..."
IOS_SIM_ARM_DIR="$BUILD_DIR/ios-simulator-arm64"
mkdir -p "$IOS_SIM_ARM_DIR"

/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/clang \
    -arch arm64 \
    -isysroot /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk \
    -mios-simulator-version-min=12.0 \
    -I"$CMARK_INCLUDES" \
    -DCMARK_GFM_STATIC_DEFINE=1 \
    -DCMARK_GFM_EXTENSIONS_STATIC_DEFINE=1 \
    -std=c99 \
    -O2 \
    -c \
    $C_FILES \
    -working-directory "$IOS_SIM_ARM_DIR"

ar rcs "$IOS_SIM_ARM_DIR/libcmark_gfm.a" "$IOS_SIM_ARM_DIR"/*.o
ranlib "$IOS_SIM_ARM_DIR/libcmark_gfm.a"

echo "✅ iOS Simulator arm64 library created"

# Create universal simulator library using lipo
echo "🔧 Creating universal simulator library..."
IOS_SIM_UNIVERSAL_DIR="$BUILD_DIR/ios-simulator-universal"
mkdir -p "$IOS_SIM_UNIVERSAL_DIR"

lipo -create \
    "$IOS_SIM_X86_DIR/libcmark_gfm.a" \
    "$IOS_SIM_ARM_DIR/libcmark_gfm.a" \
    -output "$IOS_SIM_UNIVERSAL_DIR/libcmark_gfm.a"

echo "✅ Universal simulator library created"

# Verify architectures
echo "🔍 Verifying architectures:"
echo "iOS Device:"
lipo -info "$IOS_DEVICE_DIR/libcmark_gfm.a"
echo "iOS Simulator Universal:"
lipo -info "$IOS_SIM_UNIVERSAL_DIR/libcmark_gfm.a"

# Create XCFramework
echo "📦 Creating XCFramework..."
mkdir -p "$(dirname "$XCFRAMEWORK_OUTPUT")"

xcodebuild -create-xcframework \
    -library "$IOS_DEVICE_DIR/libcmark_gfm.a" \
    -headers "$CMARK_INCLUDES" \
    -library "$IOS_SIM_UNIVERSAL_DIR/libcmark_gfm.a" \
    -headers "$CMARK_INCLUDES" \
    -output "$XCFRAMEWORK_OUTPUT"

echo "✅ cmark_gfm XCFramework built successfully!"
echo "📍 Location: $XCFRAMEWORK_OUTPUT"

# Verify the XCFramework
echo "🔍 XCFramework contents:"
ls -la "$XCFRAMEWORK_OUTPUT"
if [ -f "$XCFRAMEWORK_OUTPUT/Info.plist" ]; then
    echo "📋 XCFramework Info:"
    plutil -p "$XCFRAMEWORK_OUTPUT/Info.plist" | head -20
fi