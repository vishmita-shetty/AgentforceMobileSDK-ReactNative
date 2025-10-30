# React Native Agentforce Specification

## Overview

This application demonstrates the integration of Salesforce Agentforce SDK within a React Native mobile application (iOS and Android), providing AI-powered conversational capabilities with seamless navigation between Agentforce chat and native app screens. Full feature parity across both platforms.

## Core Features

### 1. Agentforce SDK Integration

- Native iOS and Android bridges connecting React Native to Salesforce Agentforce SDKs
- Full initialization and configuration of Agentforce client with Salesforce credentials
- Support for multi-agent mode and agent picker UI
- Cross-platform event emission for navigation interception

### 2. User Interface

**Main Screen (Contact List)**
- Clean, modern contact list with card-based design
- Each contact displays: avatar with initials, name, title, and company
- Header showing "Contacts" title and contact count
- "Settings" button in upper-right corner
- Agentforce floating action button (bottom-right)

**Settings Screen**
- Accessible via "Settings" button in header
- Agent management: add, edit, delete agents with IDs and labels
- Set default agent for conversations
- Organization ID display (auto-retrieved from Salesforce)
- Record ID input field for conversation context
- Auto-saves on back navigation

**Contact Detail Screen**
- Full contact information display
- Record ID with copy-to-clipboard functionality
- Email, phone, mobile, title, department, and account details

### 3. Agent Configuration

- Dynamic agent management with CRUD operations
- Real-time re-initialization of SDK when configuration changes

### 4. Context Management

- Automatic Salesforce Organization ID retrieval
- **iOS:** Configurable Record ID for conversation context via `RecordIdContextVariable`
- **Android:** Configurable Record ID via `getCurrentContextManager()` and `updatePageContext()`
- Both platforms allow setting record context before starting conversations

### 5. Navigation Event Interception

- Event bridge system between Agentforce SDK and React Native
- Intercepts navigation events when user taps record links in Agentforce chat
- Validates if navigation target is a loaded Contact record
- Automatically dismisses Agentforce chat panel
- Redirects user to native Contact detail screen in React Native
- Displays alert if record is not in loaded list

## Technical Architecture

### Native iOS Layer
- **AgentforceManager**: Objective-C bridge module extending RCTEventEmitter for event emission
- **AgentforceClientManager**: Swift class managing SDK lifecycle and configuration
- **SalesforceNavigationService**: Custom navigation service intercepting Agentforce navigation events

### Native Android Layer
- **AgentforceManagerModule**: Java bridge module with event emitter support
- **AgentforceClientManager**: Kotlin class managing SDK lifecycle, configuration, and navigation
- **SalesforceNavigationService**: Kotlin implementation of Navigation interface for intercepting events
- **Key Implementation Details:**
  - Uses `AgentforceMode.FullConfig` (workaround for EmployeeAgent mode SDK bug)
  - Feature flag: `enableNavAndQuickFollowUpAction = true`
  - Navigation dependency: `com.salesforce.android.mobile.interfaces:navigation:0.2.0`

### React Native Layer
- **ContactListScreen**: Main screen displaying enhanced contact list with avatars and Settings navigation
- **SettingsScreen**: Configuration screen for agents, organization ID, and record context (auto-saves on exit)
- **ContactDetailScreen**: Detail view for individual contacts with clipboard integration
- **Event Listener**: NativeEventEmitter listening for navigation events from native code

### Data Flow
1. User authenticates with Salesforce using SalesforceSDK
2. App queries Organization ID and Contact records via SOQL
3. Agentforce SDK initialized with agent configuration, org ID, and endpoint
4. User launches Agentforce chat with optional Record ID context
5. When user taps contact link in chat:
   - Agentforce navigation service intercepts the event
   - Event is emitted to React Native via event bridge
   - React Native dismisses Agentforce panel
   - React Native navigates to ContactDetail screen

## Key Technologies

- React Native (TypeScript)
- Salesforce Agentforce iOS SDK
- Salesforce Agentforce Android SDK (14.20.0-experimental)
- Salesforce Mobile SDK (SalesforceSDKCore, SalesforceReact, SalesforceAnalytics)
- React Navigation (Stack Navigator)
- Native Modules:
  - iOS: Objective-C/Swift bridge
  - Android: Java/Kotlin bridge
- SMI Navigation Interface Library (Android)
- Event Emitters (Native to JavaScript communication)

## Known Issues & Workarounds

### Android: AgentforceMode.EmployeeAgent Bug
**Issue:** When using `AgentforceMode.EmployeeAgent`, the SDK does not copy the `navigation` setting during configuration processing (AgentforceClient.kt lines 253-272).

**Workaround:** Use `AgentforceMode.FullConfig(config)` instead. This mode preserves all configuration settings including navigation.

**Impact:** None when using FullConfig mode. Navigation works correctly.
