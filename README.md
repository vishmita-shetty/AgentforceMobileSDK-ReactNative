# Agentforce Service Agent Sample App (React Native)

A lightweight React Native sample application demonstrating Service Agent integration with the Agentforce Mobile SDK for both Android and iOS.

## ✨ Features

- **Lightweight**: Simplified 2-screen app (Home + Settings)
- **No Authentication**: Service Agent mode requires no user authentication
- **Simple Configuration**: Only 3 fields needed to get started
- **Cross-Platform**: Full iOS and Android support
- **Native SDK UI**: Uses pre-built Agentforce conversation interface

## 📋 Requirements

- **React Native**: 0.74.7+
- **Node.js**: 18+
- **Android**: SDK 29+ (Android 10+)
- **iOS**: 17.0+
- **Xcode**: 15+ (for iOS development)
- **Android Studio**: Latest (for Android development)

## 🚀 Quick Start

### 1. Clone and Install

```bash
git clone <repository-url>
cd AgentforceSDK-ReactNative
npm install
```

### 2. Configure Service Agent

You'll need three values from your Salesforce org:

- **Service API URL**: Your Salesforce instance URL (e.g., `https://your-domain.my.salesforce.com`)
- **Organization ID**: Your 15 or 18 character Salesforce Org ID
- **ES Developer Name**: The API name of your Einstein Service Agent

### 3. Run the App

**Android:**
```bash
npm run android
```

**iOS:**
```bash
cd ios
pod install
cd ..
npm run ios
```

### 4. Configure in the App

1. Launch the app
2. Tap **Settings**
3. Enter your three configuration values
4. Tap **Save Configuration**
5. Return to home and tap **Launch Agentforce**

## 📱 App Structure

```
├── App.tsx                      # Main navigation setup
├── src/
│   ├── screens/
│   │   ├── HomeScreen.tsx       # Main screen with launch button
│   │   └── SettingsScreen.tsx   # Configuration screen
│   └── services/
│       └── AgentforceService.ts # Native module bridge
├── android/
│   └── app/src/main/java/.../reactagentforce/
│       ├── ServiceAgentModule.kt           # React Native bridge
│       ├── ServiceAgentViewModel.kt        # SDK state management
│       ├── ServiceAgentConversationActivity.kt # Compose UI
│       └── ServiceAgentPackage.kt          # Module registration
└── ios/
    └── ReactAgentforce/ServiceAgent/
        ├── ServiceAgentModule.swift        # ViewModel & auth provider
        ├── ServiceAgentBridge.m            # Bridge header
        └── ServiceAgentBridge.swift        # Bridge implementation
```

## 🔧 Configuration Details

### Service API URL
The base URL of your Salesforce instance. Find this in Setup → Company Information → Instance Name.

Example: `https://mycompany.my.salesforce.com`

### Organization ID
Your unique Salesforce organization identifier. Find this in Setup → Company Information → Organization ID.

Example: `00D000000000000AAA`

### ES Developer Name
The API name of your Einstein Service Agent configuration. Find this in Setup → Einstein Service Agents.

Example: `My_Service_Agent`

## 🏗️ Architecture

### React Native Layer
- **HomeScreen**: Displays configuration status and launch button
- **SettingsScreen**: Simple form for three configuration fields
- **AgentforceService**: TypeScript wrapper for native modules

### Android Native Layer
- **ServiceAgentModule**: Bridge between React Native and SDK
- **ServiceAgentViewModel**: Manages SDK lifecycle and state
- **ServiceAgentConversationActivity**: Jetpack Compose UI hosting

### iOS Native Layer
- **ServiceAgentModule**: SwiftUI-based ViewModel managing state
- **ServiceAgentBridge**: Objective-C/Swift bridge to React Native
- **ConversationView**: SwiftUI wrapper for SDK UI

## 🔄 What Changed from Employee Agent?

This app was simplified from an Employee Agent demo to Service Agent:

| Aspect | Employee Agent (Before) | Service Agent (After) |
|--------|------------------------|----------------------|
| **Authentication** | OAuth with Salesforce user login | No authentication required |
| **Configuration** | Complex: agents array, auth, endpoints | Simple: 3 fields only |
| **Dependencies** | Mobile SDK + Agentforce SDK | Agentforce SDK only |
| **Code Lines** | ~1,400 (JS) + heavy native | ~400 (JS) + minimal native |
| **Screens** | 3+ (Contacts, Details, Settings) | 2 (Home, Settings) |
| **Use Case** | Internal employees | External customers/service |

## 🧹 Removed Features

To keep this sample lightweight, the following were removed:

- ❌ Salesforce Mobile SDK
- ❌ OAuth authentication flows
- ❌ Contact list and SOQL queries
- ❌ Complex agent management (CRUD)
- ❌ Navigation event interception
- ❌ Multiple conversation management
- ❌ Data persistence layers
- ❌ react-native-force dependency

## 🐛 Troubleshooting

### Android

**Issue**: Build fails with dependency conflicts
```bash
cd android
./gradlew clean cleanBuildCache
cd ..
npm run android
```

**Issue**: "SDK not configured" error
- Verify all three fields are filled in Settings
- Check console logs for initialization errors

### iOS

**Issue**: Pod install fails
```bash
cd ios
pod deintegrate
pod install
cd ..
```

**Issue**: Module 'AgentforceModule' not found
- Clean build folder in Xcode (Cmd+Shift+K)
- Rebuild the app

## 📊 Performance

- **APK Size**: ~8-10 MB (vs ~20 MB with Mobile SDK)
- **Launch Time**: <2 seconds on modern devices
- **Memory**: Minimal overhead, SDK handles conversation state

## 🔐 Security

- No user credentials stored in the app
- Service Agent uses organizational configuration only
- All communication over HTTPS with Salesforce
- Follow Salesforce security best practices for your org

## 📚 Additional Resources

- [Agentforce SDK Documentation](https://developer.salesforce.com/docs/agentforce)
- [Service Agent Setup Guide](https://help.salesforce.com/einstein-service)
- [React Native Documentation](https://reactnative.dev/)

## 🤝 Contributing

This is a sample/demo application. For production use, consider:
- Adding configuration persistence (AsyncStorage)
- Implementing error recovery
- Adding analytics/logging
- Custom theming support
- Localization

## 📄 License

See LICENSE file for details.

## 🆘 Support

For issues with:
- **Agentforce SDK**: Contact Salesforce Support
- **This Sample App**: File an issue in this repository
- **React Native**: See React Native documentation

---

**Note**: This is a sample application intended for demonstration purposes. Customize according to your production requirements.
