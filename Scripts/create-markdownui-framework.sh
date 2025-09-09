#!/bin/bash

# Create a basic MarkdownUI framework that provides the interface AgentforceSDK expects
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_DIR="$PROJECT_ROOT/build/markdownui-framework"
XCFRAMEWORK_OUTPUT="$PROJECT_ROOT/ios/Frameworks/MarkdownUI.xcframework"
CMARK_FRAMEWORK="$PROJECT_ROOT/ios/Frameworks/cmark_gfm.xcframework"

echo "🔨 Creating basic MarkdownUI XCFramework..."

# Clean previous builds
rm -rf "$BUILD_DIR"
rm -rf "$XCFRAMEWORK_OUTPUT"
mkdir -p "$BUILD_DIR"

# Check dependencies
if [ ! -d "$CMARK_FRAMEWORK" ]; then
    echo "❌ Error: cmark_gfm.xcframework not found."
    exit 1
fi

# Create basic Swift source that provides the interface AgentforceSDK expects
SWIFT_SOURCE_DIR="$BUILD_DIR/Sources"
mkdir -p "$SWIFT_SOURCE_DIR"

# Create a minimal MarkdownUI module
cat > "$SWIFT_SOURCE_DIR/MarkdownUI.swift" << 'EOF'
import Foundation

// Basic MarkdownUI framework to satisfy AgentforceSDK dependencies
// This provides the interface without full implementation

@objc public class MarkdownRenderer: NSObject {
    @objc public static let shared = MarkdownRenderer()
    
    @objc public func renderMarkdown(_ text: String) -> NSAttributedString {
        // Basic implementation - just return the plain text
        return NSAttributedString(string: text)
    }
}

public struct MarkdownConfiguration {
    public init() {}
}

public protocol MarkdownRendering {
    func render(_ markdown: String) -> NSAttributedString
}

public class BasicMarkdownRenderer: MarkdownRendering {
    public init() {}
    
    public func render(_ markdown: String) -> NSAttributedString {
        return NSAttributedString(string: markdown)
    }
}
EOF

echo "📝 Created basic MarkdownUI Swift source"

# Build for iOS Device (arm64)
echo "📱 Building MarkdownUI for iOS Device (arm64)..."
IOS_DEVICE_DIR="$BUILD_DIR/ios-device"
mkdir -p "$IOS_DEVICE_DIR"

# Extract headers from cmark_gfm XCFramework for iOS
CMARK_IOS_HEADERS="$CMARK_FRAMEWORK/ios-arm64/Headers"
CMARK_IOS_LIB="$CMARK_FRAMEWORK/ios-arm64/libcmark_gfm.a"

# Build Swift framework for iOS Device
swiftc \
    -module-name MarkdownUI \
    -emit-library \
    -emit-module \
    -emit-module-path "$IOS_DEVICE_DIR/MarkdownUI.swiftmodule" \
    -target arm64-apple-ios12.0 \
    -sdk /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk \
    -I "$CMARK_IOS_HEADERS" \
    -O \
    -whole-module-optimization \
    "$SWIFT_SOURCE_DIR/MarkdownUI.swift" \
    -o "$IOS_DEVICE_DIR/libMarkdownUI.a"

echo "✅ iOS Device library created"

# Build for iOS Simulator (x86_64 + arm64)  
echo "📱 Building MarkdownUI for iOS Simulator..."
IOS_SIM_DIR="$BUILD_DIR/ios-simulator"
mkdir -p "$IOS_SIM_DIR"

CMARK_SIM_HEADERS="$CMARK_FRAMEWORK/ios-arm64_x86_64-simulator/Headers"

# Build for x86_64 simulator
swiftc \
    -module-name MarkdownUI \
    -emit-library \
    -emit-module \
    -emit-module-path "$IOS_SIM_DIR/MarkdownUI.swiftmodule" \
    -target x86_64-apple-ios12.0-simulator \
    -sdk /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk \
    -I "$CMARK_SIM_HEADERS" \
    -O \
    -whole-module-optimization \
    "$SWIFT_SOURCE_DIR/MarkdownUI.swift" \
    -o "$IOS_SIM_DIR/libMarkdownUI_x86_64.a"

# Build for arm64 simulator  
swiftc \
    -module-name MarkdownUI \
    -emit-library \
    -target arm64-apple-ios12.0-simulator \
    -sdk /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk \
    -I "$CMARK_SIM_HEADERS" \
    -O \
    -whole-module-optimization \
    "$SWIFT_SOURCE_DIR/MarkdownUI.swift" \
    -o "$IOS_SIM_DIR/libMarkdownUI_arm64.a"

# Create universal simulator library
lipo -create \
    "$IOS_SIM_DIR/libMarkdownUI_x86_64.a" \
    "$IOS_SIM_DIR/libMarkdownUI_arm64.a" \
    -output "$IOS_SIM_DIR/libMarkdownUI.a"

echo "✅ iOS Simulator universal library created"

# Create XCFramework
echo "📦 Creating MarkdownUI XCFramework..."
mkdir -p "$(dirname "$XCFRAMEWORK_OUTPUT")"

xcodebuild -create-xcframework \
    -library "$IOS_DEVICE_DIR/libMarkdownUI.a" \
    -headers "$IOS_DEVICE_DIR" \
    -library "$IOS_SIM_DIR/libMarkdownUI.a" \
    -headers "$IOS_SIM_DIR" \
    -output "$XCFRAMEWORK_OUTPUT"

echo "✅ MarkdownUI XCFramework created successfully!"
echo "📍 Location: $XCFRAMEWORK_OUTPUT"

# Verify the XCFramework
echo "🔍 XCFramework contents:"
ls -la "$XCFRAMEWORK_OUTPUT"