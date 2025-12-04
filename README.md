# Agentforce SDK React Native Sample App

A lightweight React Native sample application demonstrating **Service Agent** integration with Salesforce Agentforce SDK for both **iOS** and **Android**.

## 🎯 What is a Service Agent?

Service Agent is a **lightweight mode** of Agentforce that:
- ✅ **No Mobile SDK required** - Pure Agentforce SDK integration
- ✅ **Guest user authentication** - No complex user management
- ✅ **Simpler setup** - Fewer dependencies and configuration
- ✅ **Perfect for customer service** - Anonymous user support scenarios

This differs from Employee Agent mode which requires full Salesforce Mobile SDK authentication.

## 🏗️ Architecture

### Android (Service Agent - Lightweight)
- **Native Layer**: Kotlin + Jetpack Compose
- **Pattern**: Singleton `AgentforceClientHolder` for lifecycle management
- **UI**: Native `ServiceAgentConversationActivity` with Compose
- **Auth**: Guest user credential provider

### iOS (Service Agent - Lightweight)
- **Native Layer**: Swift + SwiftUI
- **Pattern**: Singleton `ServiceAgentManager` for lifecycle management
- **UI**: Native `AgentforceConversationContainer` with SwiftUI
- **Auth**: Guest user credential provider

### JavaScript Layer (Common)
- **Framework**: React Native + TypeScript
- **Navigation**: React Navigation
- **Bridge**: Native modules for SDK initialization and conversation launch
- **Screens**: Home, Settings, About

## 📋 Prerequisites

### General
- Node.js 18+ and npm/yarn
- Git

### Android
- Android Studio
- Android SDK 24+
- Gradle 8.0+

### iOS
- macOS
- Xcode 15+
- CocoaPods
- iOS 17.0+

## 🚀 Quick Start

### 1. Clone and Install

```bash
# Clone the repository
git clone <repository-url>
cd AgentforceSDK-ReactNative

# Install JavaScript dependencies
npm install
```

### 2. iOS Setup

```bash
# Run iOS setup script (installs pods)
node installios.js

# Run on iOS
npm run ios
```

### 3. Android Setup

```bash
# Android setup is automatic with Gradle
# Just run the app directly
npx react-native run-android
```

## ⚙️ Configuration

### Service Agent Settings

When you first launch the app, navigate to **Settings** and configure:

1. **Organization URL** (required)
   - Example: `https://your-org.my.salesforce.com`

2. **API Name** (required)
   - The API name of your Service Agent in Salesforce
   - Example: `My_Service_Agent`

3. **Developer Name** (required)
   - The developer name of your Service Agent
   - Example: `My_Service_Agent`

4. **Site URL** (required)
   - Your Experience Cloud site URL
   - Example: `https://your-site.force.com`

5. **Save** the configuration

### Testing the Conversation

1. From the Home screen, tap **Launch Agentforce**
2. The SDK will initialize with your configuration
3. Native conversation UI will appear
4. Start chatting with your Service Agent!

## 📁 Project Structure

```
AgentforceSDK-ReactNative/
├── src/                           # React Native JavaScript/TypeScript
│   ├── screens/
│   │   ├── HomeScreen.tsx         # Home screen with launch button
│   │   ├── SettingsScreen.tsx    # Service Agent configuration
│   │   └── AboutScreen.tsx        # App information
│   ├── services/
│   │   └── AgentforceService.ts  # Bridge wrapper for native modules
│   └── types/
│       └── agentforce.types.ts   # TypeScript types
├── android/                       # Android native code
│   └── app/src/main/
│       ├── java/.../agentforce/
│       │   ├── AgentforceModule.kt              # RN bridge
│       │   ├── AgentforceClientHolder.kt        # Singleton manager
│       │   ├── ServiceAgentConversationActivity.kt  # Compose UI
│       │   └── ServiceAgentCredentialProvider.kt    # Auth
│       └── AndroidManifest.xml
├── ios/                           # iOS native code
│   └── ReactAgentforce/
│       ├── Agentforce/
│       │   ├── AgentforceModule.swift           # RN bridge
│       │   ├── AgentforceModule.m               # ObjC bridge
│       │   ├── ServiceAgentManager.swift        # Singleton manager
│       │   └── ServiceAgentCredentialProvider.swift # Auth
│       ├── AppDelegate.h
│       └── AppDelegate.m
└── App.tsx                        # Root component with navigation
```

## 🔧 Development

### Running on Android

```bash
# Start Metro bundler
npm start

# In another terminal, run Android
npx react-native run-android

# View logs
npx react-native log-android
```

### Running on iOS

```bash
# Start Metro bundler
npm start

# In another terminal, run iOS
npm run ios

# View logs
npx react-native log-ios
```

### Debugging

- **React Native Debugger**: Use Flipper or React Native Debugger
- **Native Android**: Use Android Studio logcat
- **Native iOS**: Use Xcode console
- **JavaScript logs**: Check Metro bundler output

## 🧪 Testing

### Manual Testing Flow

1. **Configuration Test**
   - Open Settings
   - Fill in all Service Agent fields
   - Save and verify success message

2. **Initialization Test**
   - Return to Home screen
   - Tap "Launch Agentforce"
   - Verify "Initializing..." message appears

3. **Conversation Test**
   - Wait for conversation UI to load
   - Send a test message
   - Verify response from Service Agent

4. **Error Handling Test**
   - Try launching without configuration
   - Verify appropriate error message

## 🐛 Troubleshooting

### iOS Issues

**Pod install fails**
```bash
cd ios
pod deintegrate
pod cache clean --all
pod install --repo-update
```

**Build fails in Xcode**
```bash
# Clean build folder
cd ios
xcodebuild clean -workspace ReactAgentforce.xcworkspace -scheme ReactAgentforce
# Rebuild
npm run ios
```

### Android Issues

**Gradle sync fails**
```bash
cd android
./gradlew clean
./gradlew build
```

**App crashes on launch**
- Check Android logcat: `npx react-native log-android`
- Verify SDK dependencies in `build.gradle`
- Ensure minimum SDK version is 24+

### JavaScript Issues

**Metro bundler errors**
```bash
npm start -- --reset-cache
```

**Navigation not working**
- Verify `react-native-gesture-handler` is imported in `index.js`
- Check that `GestureHandlerRootView` wraps the app

## 📚 Key Differences from Employee Agent

| Feature | Service Agent (This App) | Employee Agent |
|---------|-------------------------|----------------|
| **Authentication** | Guest user / Token-based | Full Salesforce OAuth |
| **Mobile SDK** | ❌ Not required | ✅ Required |
| **Setup Complexity** | Low | High |
| **Use Cases** | Customer service, public support | Employee-facing apps |
| **Dependencies** | Agentforce SDK only | Agentforce + Mobile SDK |

## 🔐 Security Considerations

### Service Agent Mode
- Uses **guest user authentication** - suitable for public-facing scenarios
- Credentials are managed per-conversation
- No persistent user sessions
- Suitable for customer service use cases

### Important Notes
- **Not for sensitive data**: Service Agent mode is designed for public support scenarios
- **For employee apps**: Consider using Employee Agent mode with full Mobile SDK
- **Production deployment**: Follow Salesforce security best practices

## 📖 Additional Resources

- [Agentforce SDK Documentation](https://developer.salesforce.com/docs/agentforce)
- [React Native Documentation](https://reactnative.dev/docs/getting-started)
- [Salesforce Mobile SDK](https://developer.salesforce.com/docs/platform/mobile-sdk)

## 🤝 Contributing

This is a sample application for demonstration purposes. For production use:
1. Add comprehensive error handling
2. Implement proper state management (Redux/MobX)
3. Add unit and integration tests
4. Implement logging and analytics
5. Follow security best practices

## 📄 License

Copyright (c) 2024-present, salesforce.com, inc. All rights reserved.

See LICENSE file for full details.

## 🆘 Support

For issues and questions:
1. Check the troubleshooting section above
2. Review Salesforce Developer documentation
3. Raise an issue in this repository
