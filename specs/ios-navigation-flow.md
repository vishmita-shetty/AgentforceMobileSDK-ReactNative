# iOS Navigation Flow Documentation

**Platform:** iOS
**Last Updated:** October 30, 2024
**SDK Version:** Salesforce Agentforce iOS SDK

---

## Overview

This document details the complete navigation event flow when a user taps a contact link in the Agentforce chat interface on iOS. The event flows through multiple layers from the native iOS SDK through Objective-C bridge to React Native JavaScript, ultimately dismissing the chat and navigating to the contact detail screen.

---

## Complete Navigation Flow: 9 Steps

### Step 1: User Action - Tap Contact Link

**Location:** Agentforce chat interface
**Thread:** Main Thread
**Trigger:** User taps a contact record card or link within the Agentforce chat

The Agentforce SDK internally detects this tap and prepares to trigger navigation.

---

### Step 2: Agentforce SDK → SalesforceNavigationService.go()

**File:** `ios/ReactAgentforce/Agentforce/AgentforceNavigationService.swift`
**Thread:** Main Thread
**Lines:** 50-60

```swift
func go(to destination: any SalesforceNavigation.Destination, replace: Bool) {
    print("Navigation to: \(destination), replace: \(replace)")

    // Check if destination is a Record
    if let record = destination as? SalesforceNavigation.Record {
        print("Navigating to Record - ID: \(record.id), Type: \(record.type ?? "unknown")")

        // Call the navigation callback with record ID and type
        onNavigate?(record.id, record.type)
    }
}
```

**Input Data:**
- `destination: any SalesforceNavigation.Destination` - Protocol type using existential type
  - Cast to `SalesforceNavigation.Record`
  - `record.id` = `"003EE000001fkH0QAI"` (Contact record ID)
  - `record.type` = `"Contact"` (Salesforce object type, optional)
- `replace: Bool` = `false` (whether to replace current screen)

**Action:**
- Type-checks if destination is a `Record` using optional cast
- Extracts `id` and `type` (nullable) from the Record
- Invokes the closure callback `onNavigate` with these parameters

**iOS-Specific:**
- Uses Swift protocol `SalesforceNavigation.Navigation`
- Uses `any` keyword for existential types (Swift 5.7+)
- Optional type on `record.type` (can be nil)

**Data Flow:**
```
SalesforceNavigation.Record → Cast & Extract (id, type?) → Closure callback
```

---

### Step 3: Navigation Closure → AgentforceClientManager

**File:** `ios/ReactAgentforce/Agentforce/AgentforceClientManager.swift`
**Thread:** Main Thread
**Lines:** 56-69 (closure defined during initialization)

```swift
let navigationService = SalesforceNavigationService { [weak self] recordId, recordType in
    guard let self = self, let emitter = self.eventEmitter else { return }

    // Create event data dictionary
    var eventData: [String: Any] = ["id": recordId]
    if let type = recordType {
        eventData["type"] = type
    }

    // Call emitNavigationEvent on the event emitter
    if emitter.responds(to: Selector(("emitNavigationEvent:"))) {
        emitter.perform(Selector(("emitNavigationEvent:")), with: eventData)
    }
}
```

**Input Data:**
- `recordId: String` = `"003EE000001fkH0QAI"`
- `recordType: String?` = `"Contact"` (nullable)

**Action:**
- Closure captures `self` weakly to prevent retain cycles
- Captures reference to `eventEmitter` (the `AgentforceManager` instance)
- Creates dictionary with "id" key
- Conditionally adds "type" key if recordType is not nil
- Uses Objective-C runtime to call method on emitter
- Checks if method exists before calling with `responds(to:)`

**iOS-Specific:**
- Uses `[weak self]` capture list for memory management
- Uses `guard let` for safe unwrapping
- Bridges to Objective-C using `Selector()` and `perform(_:with:)`
- Dictionary automatically bridges to `NSDictionary`

**Data Transformation:**
```swift
Swift (recordId: String, recordType: String?)
  → Dictionary ["id": String, "type": String?]
    → NSDictionary (Objective-C)
```

---

### Step 4: Bridge to Objective-C → AgentforceManager.emitNavigationEvent

**File:** `ios/ReactAgentforce/AgentforceManager.m`
**Thread:** Main Thread
**Lines:** 47-50

```objc
- (void)emitNavigationEvent:(NSDictionary *)eventData
{
    [self sendEventWithName:@"agentforceNavigation" body:eventData];
}
```

**Input Data:**
- `eventData: NSDictionary` = `@{@"id": @"003EE000001fkH0QAI", @"type": @"Contact"}`

**Action:**
- Objective-C method receives NSDictionary from Swift
- Calls React Native event emitter method `sendEventWithName:body:`
- This is inherited from `RCTEventEmitter` base class

**iOS-Specific:**
- Objective-C acts as bridge between Swift and React Native
- Uses React Native's event emitter infrastructure
- Event name: `@"agentforceNavigation"`

**Data Transformation:**
```objc
NSDictionary → React Native Event Body (JSON serialization)
```

---

### Step 5: React Native Bridge → JavaScript (Bridge Crossing)

**Context:** React Native bridge internal processing
**Thread:** Main Thread → Bridge Queue → **JavaScript Thread** (Bridge Crossing #1)

**What Happens:**
- `RCTEventEmitter.sendEventWithName:body:` serializes the event
- Event is queued on the React Native bridge
- Bridge transfers data from native to JavaScript
- Event is delivered to registered listeners

**Data Serialization:**
```
NSDictionary → JSON → JavaScript Object
{
  id: "003EE000001fkH0QAI",
  type: "Contact"
}
```

**Bridge Crossing:** Native (Main Thread) → JavaScript (JS Thread)

---

### Step 6: JavaScript Event Listener Receives Event

**File:** `app.tsx`
**Thread:** JavaScript Thread
**Lines:** 152-174

```typescript
this.navigationEventSubscription = this.eventEmitter.addListener(
    'agentforceNavigation',
    (event: { id: string; type?: string }) => {
        // Search for the contact in our data
        const contact = this.state.data.find((c) => c.Id === event.id);

        // Check if this is a Contact record
        if (event.type === 'Contact') {
            if (contact) {
                // Dismiss the Agentforce chat view, then navigate
                AgentforceManager.dismissAgentforceChatView()
                    .then(() => {
                        this.props.navigation.navigate('ContactDetail', { contact });
                    })
                    .catch((error: any) => {
                        console.error('Failed to dismiss Agentforce chat:', error);
                    });
            } else {
                Alert.alert('Contact Not Found', 'This contact is not in the loaded list.');
            }
        }
    }
);
```

**Input Data:**
```typescript
event = {
    id: "003EE000001fkH0QAI",
    type: "Contact"
}
```

**Action:**
1. Event listener receives the event object
2. Searches component state for contact with matching `event.id`
3. Validates that `event.type === 'Contact'`
4. If contact found, calls native `dismissAgentforceChatView()` method
5. If contact not found, displays alert to user

**Data Lookup:**
```typescript
const contact = this.state.data.find((c) => c.Id === event.id)
// Returns full contact object: {Id, Name, Email, Phone, Title, etc.}
```

---

### Step 7: Call Native Dismiss Method (Bridge Crossing)

**File:** `ios/ReactAgentforce/AgentforceManager.m`
**Thread:** JavaScript Thread → **Main Thread** (Bridge Crossing #2)
**Lines:** 111-132

```objc
RCT_EXPORT_METHOD(dismissAgentforceChatView:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        @try {
            if (!self.agentforceClient) {
                reject(@"NOT_INITIALIZED", @"AgentforceClient not initialized.", nil);
                return;
            }

            [self.agentforceClient dismissChatViewWithCompletion:^(NSError * _Nullable error) {
                if (error) {
                    reject(@"DISMISS_ERROR", error.localizedDescription, error);
                } else {
                    resolve(@{@"success": @YES});
                }
            }];
        } @catch (NSException *exception) {
            reject(@"DISMISS_EXCEPTION", exception.reason, nil);
        }
    });
}
```

**Action:**
- JavaScript calls native module method via bridge
- Method is exported with `RCT_EXPORT_METHOD` macro
- Dispatches work to main queue using `dispatch_async(dispatch_get_main_queue(), ^{...})`
- Calls Swift's `dismissChatViewWithCompletion:` method
- Returns a Promise to JavaScript

**iOS-Specific:**
- Uses Grand Central Dispatch (GCD) for threading
- Explicit dispatch to main queue for UI operations
- Promise-based API (resolve/reject blocks)
- Objective-C blocks for async callbacks

**Bridge Crossing:** JavaScript (JS Thread) → Native (Main Thread via GCD)

---

### Step 8: Dismiss UI Coordinator

**File:** `ios/ReactAgentforce/Agentforce/AgentforceClientManager.swift`
**Thread:** Main Thread
**Lines:** 195-197

```swift
@objc public func dismissChatView(completion: @escaping (Error?) -> Void) {
    uiCoordinator.dismissChatView(completion: completion)
}
```

**Then:**
**File:** `ios/ReactAgentforce/Agentforce/AgentforceUICoordinator.swift`
**Lines:** 64-76

```swift
func dismissChatView(completion: @escaping (Error?) -> Void) {
    DispatchQueue.main.async { [weak self] in
        if let chatViewController = self?.currentChatViewController {
            chatViewController.dismiss(animated: true) {
                self?.currentChatViewController = nil
                self?.agentforceDelegate = nil
                completion(nil)
            }
        } else {
            completion(nil)
        }
    }
}
```

**Action:**
- Gets the UI coordinator (manages SwiftUI view presentation)
- Calls coordinator's `dismissChatView(completion:)` method
- UI coordinator dismisses the UIHostingController containing SwiftUI chat view
- Uses `.pageSheet` modal presentation style
- Animates dismissal
- Cleans up view controller and delegate references
- Calls completion callback with nil error (success)

**iOS-Specific:**
- Uses `UIHostingController` for SwiftUI integration
- Modal presentation via `.pageSheet` style
- Cleanup in dismissal completion block
- Weak self capture in async block

**UI Effect:** Agentforce chat panel is removed from screen with animation

---

### Step 9: Promise Resolution & Navigate (Bridge Crossing)

**File:** `app.tsx`
**Thread:** Main Thread → **JavaScript Thread** (Bridge Crossing #3)
**Lines:** 162-165

```typescript
AgentforceManager.dismissAgentforceChatView()
    .then(() => {
        this.props.navigation.navigate('ContactDetail', { contact });
    })
    .catch((error: any) => {
        console.error('Failed to dismiss Agentforce chat:', error);
    });
```

**Action:**
- Native layer resolves the Promise with `{success: true}`
- Promise resolution crosses bridge back to JavaScript
- `.then()` callback executes
- Calls React Navigation's `navigate()` method
- Pushes ContactDetail screen onto navigation stack

**Navigation Data:**
```typescript
{
    contact: {
        Id: "003EE000001fkH0QAI",
        Name: "John Doe",
        Email: "john.doe@example.com",
        Phone: "555-1234",
        MobilePhone: "555-5678",
        Title: "VP Sales",
        Department: "Sales",
        Account: { Name: "Acme Corp" }
    }
}
```

**Final State:**
- Navigation Stack: `ContactListScreen` → `ContactDetail`
- Agentforce chat: Dismissed
- Screen: Contact detail with full information

**Bridge Crossing:** Native (Main Thread) → JavaScript (JS Thread)

---

## Visual Flow Diagram

```
┌───────────────────────────────────────────────────────────────┐
│ STEP 1: USER TAPS CONTACT LINK IN AGENTFORCE                  │
│ Thread: Main                                                  │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 2: Agentforce SDK → SalesforceNavigationService.go()     │
│ File: AgentforceNavigationService.swift                       │
│ Data: record.id="003...", record.type="Contact"              │
│ Thread: Main                                                  │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 3: Navigation Closure → AgentforceClientManager          │
│ File: AgentforceClientManager.swift                           │
│ Action: Create Dictionary, bridge to Objective-C              │
│ Thread: Main                                                  │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 4: Bridge to Objective-C → AgentforceManager             │
│ File: AgentforceManager.m                                     │
│ Action: sendEventWithName:@"agentforceNavigation"            │
│ Thread: Main                                                  │
└────────────────────────────┬──────────────────────────────────┘
                             │
                    [BRIDGE CROSSING #1]
                    Native (Main) → JavaScript
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 5: React Native Bridge                                   │
│ Serialization: NSDictionary → JSON → JS Object               │
│ Thread: Bridge Queue → JS Thread                             │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 6: Event Listener in React Native                        │
│ File: app.tsx                                                 │
│ Action: Find contact, validate type, call dismiss             │
│ Thread: JavaScript                                            │
└────────────────────────────┬──────────────────────────────────┘
                             │
                    [BRIDGE CROSSING #2]
                    JavaScript → Native (Main via GCD)
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 7: Call Native Dismiss Method                            │
│ File: AgentforceManager.m                                     │
│ Method: RCT_EXPORT_METHOD dismissAgentforceChatView           │
│ Thread: Main (via dispatch_async)                            │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 8: Dismiss UI Coordinator                                │
│ Files: AgentforceClientManager.swift, AgentforceUICoordinator │
│ Action: UIViewController.dismiss(animated:)                   │
│ Effect: Chat panel removed from screen                        │
│ Thread: Main                                                  │
└────────────────────────────┬──────────────────────────────────┘
                             │
                    [BRIDGE CROSSING #3]
                    Native (Main) → JavaScript
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 9: Promise Resolves & Navigate                           │
│ File: app.tsx                                                 │
│ Action: navigation.navigate('ContactDetail')                  │
│ Effect: Contact detail screen displayed                       │
│ Thread: JavaScript                                            │
└───────────────────────────────────────────────────────────────┘
```

---

## Thread Summary

| Step | Thread/Queue | Notes |
|------|-------------|-------|
| 1-4 | Main Thread | Native navigation handling |
| 5 | Bridge Queue | Event serialization and transfer |
| 6 | **JavaScript Thread** | Bridge crossing #1 - Event received |
| 7 | **Main Thread** | Bridge crossing #2 - Dismiss via GCD |
| 8 | Main Thread | UI dismissal |
| 9 | **JavaScript Thread** | Bridge crossing #3 - Navigate |

**Total Bridge Crossings:** 3

**iOS-Specific Threading:**
- Uses Grand Central Dispatch (GCD) for explicit threading
- Main queue (`dispatch_get_main_queue()`) for UI operations
- `DispatchQueue.main.async` in Swift
- React Native bridge handles JS thread automatically

---

## iOS-Specific Features

### 1. RecordIdContextVariable API

**File:** `ios/ReactAgentforce/Agentforce/AgentforceClientManager.swift`
**Lines:** 159-172

```swift
if let userContext = userContext, !userContext.isEmpty {
    Task {
        do {
            let recordIdContext = RecordIdContextVariable(userContext)
            if let variable = recordIdContext.variable {
                try await conversation.setAdditionalContext(context: [variable])
                print("Successfully set record ID context: \(userContext)")
            }
        } catch {
            print("Warning: Failed to set record ID context: \(error)")
            // Continue even if setting context fails
        }
    }
}
```

**iOS-Specific:**
- Uses `RecordIdContextVariable` class from Agentforce SDK
- Creates context variable from record ID string
- Sets context asynchronously using Swift Concurrency (`async/await`)
- Wrapped in `Task` for structured concurrency
- Non-blocking error handling (continues on failure)

### 2. Swift & Objective-C Bridge

**Bridge Header:**
```objc
#import "ReactAgentforce-Swift.h"  // Auto-generated Swift bridge header
```

**Swift Exposure:**
```swift
@objc public class AgentforceClientManager: NSObject {
    @objc public func dismissChatView(completion: @escaping (Error?) -> Void)
}
```

**Pattern:**
- Objective-C exposes to React Native
- Swift implementation via `@objc` annotations
- Auto-generated bridge header connects layers
- Allows React Native → Objective-C → Swift call chain

### 3. SwiftUI Integration

**File:** `ios/ReactAgentforce/Agentforce/AgentforceUICoordinator.swift`
**Lines:** 43-60

```swift
func presentChatView<T: View>(_ chatView: T, completion: @escaping (Error?) -> Void) {
    DispatchQueue.main.async { [weak self] in
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let window = windowScene.windows.first,
           let rootViewController = window.rootViewController {

            let hostingController = UIHostingController(rootView: chatView)
            hostingController.modalPresentationStyle = .pageSheet

            self?.currentChatViewController = hostingController
            rootViewController.present(hostingController, animated: true) {
                completion(nil)
            }
        }
    }
}
```

**iOS-Specific:**
- Uses `UIHostingController` to wrap SwiftUI views
- Presents as `.pageSheet` modal (adaptive for iPhone/iPad)
- Finds root view controller through window scene
- Modern iOS 13+ scene-based architecture
- Generic over `View` protocol for flexibility

### 4. Protocol-Oriented Design

**File:** `ios/ReactAgentforce/Agentforce/AgentforceNavigationService.swift`
**Lines:** 37-60

```swift
class SalesforceNavigationService: SalesforceNavigation.Navigation {
    // Closure to handle navigation events
    var onNavigate: ((String, String?) -> Void)?

    init(onNavigate: ((String, String?) -> Void)?) {
        self.onNavigate = onNavigate
    }

    func go(to destination: any SalesforceNavigation.Destination, replace: Bool) {
        // Implementation
    }
}
```

**iOS-Specific:**
- Conforms to `SalesforceNavigation.Navigation` protocol
- Uses existential types with `any` keyword (Swift 5.7+)
- Closure-based callbacks for functional programming style
- Protocol composition allows flexible implementations

---

## Key Differences from Android

### 1. Language Stack
**iOS:** Swift → Objective-C → React Native
**Android:** Kotlin → Java → React Native

### 2. Navigation Protocol
**iOS:**
```swift
func go(to destination: any Destination, replace: Bool)
```

**Android:**
```kotlin
override fun goto(destination: Destination, replace: Boolean)
```

**Differences:**
- iOS uses `any` for existential types
- iOS uses `to` as external parameter name
- Method name: `go` vs `goto`

### 3. Context Setting
**iOS:**
```swift
let recordIdContext = RecordIdContextVariable(userContext)
try await conversation.setAdditionalContext(context: [variable])
```

**Android:**
```kotlin
val contextManager = client.getCurrentContextManager()
contextManager?.updatePageContext(AgentforcePageContext(recordId, apiName))
```

**Differences:**
- iOS uses `RecordIdContextVariable` class
- iOS uses `async/await` for context setting
- Android uses `getCurrentContextManager()` API
- Android uses `AgentforcePageContext` data class

### 4. Threading Model
**iOS:**
```swift
dispatch_async(dispatch_get_main_queue(), ^{...})
DispatchQueue.main.async { ... }
```

**Android:**
```kotlin
mainHandler.post { ... }
```

**Differences:**
- iOS uses GCD (Grand Central Dispatch)
- Android uses Handler with Looper
- iOS has explicit queue specifications
- Android uses main thread Handler pattern

### 5. Event Emission
**iOS:**
```swift
if emitter.responds(to: Selector(("emitNavigationEvent:"))) {
    emitter.perform(Selector(("emitNavigationEvent:")), with: eventData)
}
```

**Android:**
```kotlin
eventEmitter?.emitNavigationEvent(recordId, recordType ?: "")
```

**Differences:**
- iOS uses Objective-C runtime selectors
- iOS checks method existence at runtime
- Android uses direct method calls
- Android uses Elvis operator for null safety

### 6. UI Presentation
**iOS:**
```swift
let hostingController = UIHostingController(rootView: chatView)
hostingController.modalPresentationStyle = .pageSheet
rootViewController.present(hostingController, animated: true)
```

**Android:**
```kotlin
coordinator.presentChatView({
    client.AgentforceConversationContainer(conversation, onClose = {...})
}, callback)
```

**Differences:**
- iOS uses UIKit + SwiftUI hybrid
- iOS uses modal presentation styles
- Android uses Compose UI directly
- Different view lifecycle management

---

## Data Transformations

### Swift → Objective-C (Step 3)
```swift
Swift Dictionary [String: Any]
  → Automatic bridging
    → Objective-C NSDictionary
```

### Objective-C → JavaScript (Step 5)
```objc
NSDictionary
  → React Native bridge serialization
    → JSON
      → JavaScript Object
```

### JavaScript → Objective-C (Step 7)
```typescript
JavaScript function call
  → React Native bridge
    → Objective-C method invocation (RCT_EXPORT_METHOD)
```

---

## Required Configuration

### 1. Navigation Service Registration

**File:** `AgentforceClientManager.swift` (Line 131)

```swift
let config = try AgentforceConfiguration.Builder(
    credentialProvider: credentialProvider
)
// ... other configuration
.set(navigationService: navigationService)  // Register navigation service
.build()
```

### 2. Event Emitter Bridge

**File:** `AgentforceManager.h` and `AgentforceManager.m`

```objc
@interface AgentforceManager : RCTEventEmitter <RCTBridgeModule>
@end

@implementation AgentforceManager

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"agentforceNavigation"];
}
```

### 3. Swift-Objective-C Bridge

**Project Settings:**
- Bridging header: `ReactAgentforce-Bridging-Header.h`
- Auto-generated Swift header: `ReactAgentforce-Swift.h`
- `@objc` annotations on Swift classes/methods

---

## Error Handling

### Contact Not Found
```typescript
if (!contact) {
    Alert.alert('Contact Not Found', 'This contact is not in the loaded list.');
}
```

### Dismiss Failure
```typescript
.catch((error: any) => {
    console.error('Failed to dismiss Agentforce chat:', error);
});
```

### Native Module Not Initialized
```objc
if (!self.agentforceClient) {
    reject(@"NOT_INITIALIZED", @"AgentforceClient not initialized.", nil);
    return;
}
```

### Context Setting Failure
```swift
} catch {
    print("Warning: Failed to set record ID context: \(error)")
    // Continue even if setting context fails
}
```

---

## Performance Considerations

- **Bridge Crossings:** 3 total (same as Android)
- **Threading:** All UI work explicitly dispatched to main queue
- **Data Size:** Minimal (only ID and type passed initially)
- **Lookup:** O(n) contact search in state
- **Memory:** Weak captures prevent retain cycles

---

## Testing Checklist

- [ ] User taps contact link in Agentforce chat
- [ ] SalesforceNavigationService receives Record destination
- [ ] Event is emitted to React Native with correct data
- [ ] Contact is found in state
- [ ] Agentforce chat is dismissed with animation
- [ ] ContactDetail screen is displayed
- [ ] Back navigation returns to ContactList
- [ ] Contact not in list shows alert
- [ ] Error handling for dismiss failure works
- [ ] No memory leaks (check weak captures)
- [ ] Threading is correct (UI on main thread)
