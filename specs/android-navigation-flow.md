# Android Navigation Flow Documentation

**Platform:** Android
**Last Updated:** October 30, 2024
**SDK Version:** com.salesforce.android.agentforcesdk:agentforce-sdk:14.20.0-experimental

---

## Overview

This document details the complete navigation event flow when a user taps a contact link in the Agentforce chat interface on Android. The event flows through multiple layers from the native Android SDK to React Native JavaScript, ultimately dismissing the chat and navigating to the contact detail screen.

---

## Complete Navigation Flow: 10 Steps

### Step 1: User Action - Tap Contact Link

**Location:** Agentforce chat interface
**Thread:** Main/UI Thread
**Trigger:** User taps a contact record card or link within the Agentforce chat

The Agentforce SDK internally detects this tap and prepares to trigger navigation.

---

### Step 2: Agentforce SDK → SalesforceNavigationService.goto()

**File:** `android/app/src/main/java/com/salesforce/android/reactagentforce/SalesforceNavigationService.kt`
**Thread:** Main/UI Thread
**Lines:** 68-73

```kotlin
override fun goto(destination: Destination, replace: Boolean) {
    if (destination is Record) {
        onNavigate?.invoke(destination.id, destination.type)
    }
    // Note: The replace parameter is currently ignored but could be passed
    // to React Native if needed for navigation stack management
}
```

**Input Data:**
- `destination: Destination` - Cast to `Record` type
  - `destination.id` = `"003EE00000Jg3EjYAJ"` (Contact record ID)
  - `destination.type` = `"Contact"` (Salesforce object type)
- `replace: Boolean` = `false` (whether to replace current screen)

**Action:**
- Type-checks if destination is a `Record` (as opposed to Link, QuickAction, etc.)
- Extracts `id` and `type` from the Record
- Invokes the lambda callback `onNavigate` with these parameters

**Data Flow:**
```
Destination (Record) → Extract (id, type) → Lambda callback
```

---

### Step 3: Lambda Callback → AgentforceClientManager

**File:** `android/app/src/main/java/com/salesforce/android/reactagentforce/AgentforceClientManager.kt`
**Thread:** Main/UI Thread
**Lines:** 135-137

```kotlin
// Create navigation service with callback to event emitter
navigationService = SalesforceNavigationService { recordId, recordType ->
    eventEmitter?.emitNavigationEvent(recordId, recordType ?: "")
}
```

**Input Data:**
- `recordId: String` = `"003EE00000Jg3EjYAJ"`
- `recordType: String?` = `"Contact"` (nullable)

**Action:**
- Lambda function defined during initialization is invoked
- Uses Elvis operator `?:` to ensure recordType is never null (defaults to empty string)
- Calls the event emitter's `emitNavigationEvent()` method

**Data Transformation:**
```kotlin
recordType ?: ""  // Ensures non-null value
```

---

### Step 4: Event Emitter → AgentforceManagerModule.emitNavigationEvent()

**File:** `android/app/src/main/java/com/salesforce/android/reactagentforce/AgentforceManagerModule.java`
**Thread:** Main/UI Thread
**Lines:** 96-100

```java
public void emitNavigationEvent(String recordId, String recordType) {
    WritableMap params = Arguments.createMap();
    params.putString("id", recordId);
    params.putString("type", recordType);
    sendEvent("agentforceNavigation", params);
}
```

**Input Data:**
- `recordId: String` = `"003EE00000Jg3EjYAJ"`
- `recordType: String` = `"Contact"`

**Action:**
- Creates a `WritableMap` (React Native's bridge-compatible map structure)
- Adds two key-value pairs for transmission to JavaScript
- Calls `sendEvent()` with event name and params

**Data Structure Created:**
```javascript
{
  id: "003EE00000Jg3EjYAJ",
  type: "Contact"
}
```

---

### Step 5: Send Event → DeviceEventManagerModule (Bridge Crossing)

**File:** `android/app/src/main/java/com/salesforce/android/reactagentforce/AgentforceManagerModule.java`
**Thread:** Main/UI Thread → **JS Thread** (Bridge Crossing #1)
**Lines:** 82-87

```java
private void sendEvent(String eventName, @Nullable WritableMap params) {
    if (reactContext.hasActiveCatalystInstance()) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }
}
```

**Input Data:**
- `eventName: String` = `"agentforceNavigation"`
- `params: WritableMap` = `{id: "003EE00000Jg3EjYAJ", type: "Contact"}`

**Action:**
- Checks if React Native context is active (app is running)
- Gets the JS event emitter module
- Emits the event across the React Native bridge to JavaScript

**Bridge Crossing:** Native (Main Thread) → JavaScript (JS Thread)

---

### Step 6: React Native Event Listener Receives Event

**File:** `app.tsx`
**Thread:** JS Thread
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
    id: "003EE00000Jg3EjYAJ",
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

**File:** `android/app/src/main/java/com/salesforce/android/reactagentforce/AgentforceManagerModule.java`
**Thread:** JS Thread → **Main/UI Thread** (Bridge Crossing #2)
**Lines:** 254-279

```java
@ReactMethod
public void dismissAgentforceChatView(Promise promise) {
    mainHandler.post(() -> {
        try {
            if (agentforceClient == null) {
                promise.reject("NOT_INITIALIZED", "AgentforceClient not initialized.");
                return;
            }

            agentforceClient.dismissChatView(new AgentforceClientManager.DismissCallback() {
                @Override
                public void onSuccess() {
                    WritableMap result = Arguments.createMap();
                    result.putBoolean("success", true);
                    promise.resolve(result);
                }

                @Override
                public void onError(Exception error) {
                    promise.reject("DISMISS_ERROR", error.getMessage(), error);
                }
            });
        } catch (Exception e) {
            promise.reject("DISMISS_EXCEPTION", e.getMessage(), e);
        }
    });
}
```

**Action:**
- JavaScript calls native module method via bridge
- Method is annotated with `@ReactMethod` (React Native bridge method)
- Posts work to main handler (ensures UI work on main thread)
- Calls `agentforceClient.dismissChatView()` with callbacks
- Returns a Promise to JavaScript

**Bridge Crossing:** JavaScript (JS Thread) → Native (Main Thread)

---

### Step 8: UI Coordinator Dismissal

**File:** `android/app/src/main/java/com/salesforce/android/reactagentforce/AgentforceClientManager.kt`
**Thread:** Main/UI Thread
**Lines:** 275-285

```kotlin
fun dismissChatView(callback: DismissCallback) {
    try {
        val coordinator = uiCoordinator
            ?: throw IllegalStateException("UI Coordinator not initialized")

        coordinator.dismissChatView(callback)
    } catch (e: Exception) {
        Log.e(TAG, "Error dismissing chat view", e)
        callback.onError(e)
    }
}
```

**Action:**
- Gets the UI coordinator (manages Compose UI presentation)
- Calls coordinator's `dismissChatView()` method
- UI coordinator dismisses the Agentforce chat view from screen
- On success, invokes the callback

**UI Effect:** Agentforce chat panel is removed from screen

---

### Step 9: Promise Resolution & Navigate (Bridge Crossing)

**File:** `app.tsx`
**Thread:** Main/UI Thread → **JS Thread** (Bridge Crossing #3)
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

**Navigation Data:**
```typescript
{
    contact: {
        Id: "003EE00000Jg3EjYAJ",
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

**Bridge Crossing:** Native (Main Thread) → JavaScript (JS Thread)

---

### Step 10: ContactDetailScreen Renders

**File:** `app.tsx`
**Thread:** JS/Render Thread
**Lines:** 1096-1167

```typescript
class ContactDetailScreen extends React.Component<ContactDetailProps> {
    render() {
        const { contact } = this.props.route.params;

        return (
            <View style={styles.detailContainer}>
                <View style={styles.detailSection}>
                    <Text style={styles.detailLabel}>Name</Text>
                    <Text style={styles.detailValue}>{contact.Name}</Text>
                </View>

                <View style={styles.detailSection}>
                    <Text style={styles.detailLabel}>Record ID</Text>
                    <View style={styles.recordIdRow}>
                        <Text style={[styles.detailValue, styles.recordIdValue]}>
                            {contact.Id}
                        </Text>
                        <TouchableOpacity
                            style={styles.copyButton}
                            onPress={() => this.copyToClipboard(contact.Id)}
                        >
                            <Text style={styles.copyButtonText}>📋 Copy</Text>
                        </TouchableOpacity>
                    </View>
                </View>

                {/* Renders all other contact fields */}
            </View>
        );
    }
}
```

**Action:**
- React Navigation pushes `ContactDetail` screen onto navigation stack
- Screen receives contact data via route params
- Component renders all contact information
- User sees full contact details

**Final State:**
- Navigation Stack: `ContactListScreen` → `ContactDetail`
- Agentforce chat: Dismissed
- Screen: Contact detail with copy-to-clipboard functionality

---

## Visual Flow Diagram

```
┌───────────────────────────────────────────────────────────────┐
│ STEP 1: USER TAPS CONTACT LINK IN AGENTFORCE                  │
│ Thread: Main/UI                                               │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 2: Agentforce SDK → SalesforceNavigationService.goto()   │
│ File: SalesforceNavigationService.kt                          │
│ Data: destination.id="003...", destination.type="Contact"     │
│ Thread: Main/UI                                               │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 3: Lambda Callback → AgentforceClientManager             │
│ File: AgentforceClientManager.kt                              │
│ Action: eventEmitter?.emitNavigationEvent(recordId, type)     │
│ Thread: Main/UI                                               │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 4: Create WritableMap & Emit Event                       │
│ File: AgentforceManagerModule.java                            │
│ Data: WritableMap {id: "003...", type: "Contact"}            │
│ Thread: Main/UI                                               │
└────────────────────────────┬──────────────────────────────────┘
                             │
                    [BRIDGE CROSSING #1]
                    Native → JavaScript
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 5: Send Event via DeviceEventManagerModule               │
│ File: AgentforceManagerModule.java                            │
│ Event: "agentforceNavigation" with params                     │
│ Thread: JS                                                    │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 6: Event Listener Receives Event in React Native         │
│ File: app.tsx                                                 │
│ Action: Find contact in state, validate type                  │
│ Thread: JS                                                    │
└────────────────────────────┬──────────────────────────────────┘
                             │
                    [BRIDGE CROSSING #2]
                    JavaScript → Native
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 7: Call Native Dismiss Method                            │
│ File: AgentforceManagerModule.java                            │
│ Method: @ReactMethod dismissAgentforceChatView(Promise)       │
│ Thread: Main/UI (via mainHandler.post)                       │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 8: UI Coordinator Dismisses Chat View                    │
│ File: AgentforceClientManager.kt                              │
│ Action: coordinator.dismissChatView(callback)                 │
│ Effect: Agentforce panel removed from screen                  │
│ Thread: Main/UI                                               │
└────────────────────────────┬──────────────────────────────────┘
                             │
                    [BRIDGE CROSSING #3]
                    Native → JavaScript
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 9: Promise Resolves & Navigate                           │
│ File: app.tsx                                                 │
│ Action: .then(() => navigation.navigate('ContactDetail'))     │
│ Thread: JS                                                    │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌───────────────────────────────────────────────────────────────┐
│ STEP 10: ContactDetailScreen Renders                          │
│ File: app.tsx                                                 │
│ Display: Full contact information with copy functionality     │
│ Thread: JS/Render                                             │
└───────────────────────────────────────────────────────────────┘
```

---

## Thread Transitions

| Step | Thread | Notes |
|------|--------|-------|
| 1-4 | Main/UI Thread | Native navigation handling |
| 5-6 | **JS Thread** | Bridge crossing #1 - Event received in React Native |
| 7-8 | **Main/UI Thread** | Bridge crossing #2 - Dismiss native UI |
| 9-10 | **JS Thread** | Bridge crossing #3 - Navigate and render |

**Total Bridge Crossings:** 3

---

## Data Transformations

### Native → JavaScript (Step 5)
```
Kotlin Record {id, type}
  → Java WritableMap {id, type}
    → JavaScript Object {id, type}
```

### JavaScript → Native (Step 7)
```
JavaScript Promise-based method call
  → Java @ReactMethod with Promise parameter
    → Kotlin callback interface
```

### Native → JavaScript (Step 9)
```
Kotlin callback.onSuccess()
  → Java promise.resolve(WritableMap)
    → JavaScript Promise.then()
```

---

## Required Configuration

### 1. Navigation Dependency

**File:** `android/app/build.gradle`

```gradle
dependencies {
    implementation 'com.salesforce.android.mobile.interfaces:navigation:0.2.0'
}
```

### 2. Feature Flags

**File:** `AgentforceClientManager.kt` (Lines 141-154)

```kotlin
val featureFlagSettings = AgentforceFeatureFlagSettings.builder()
    .enableMultiAgent(true)
    .enableMultiModalInput(true)
    .enablePDFUpload(true)
    .enableLongPauseSpeechTranscription(true)
    .setupFlags(mapOf(
        "enableLightningTypeStreaming" to true,
        "enableNavAndQuickFollowUpAction" to true  // CRITICAL for navigation!
    ))
    .enableTheming(true)
    .enableOnboarding(false)
    .build()
```

**Critical Flag:** `enableNavAndQuickFollowUpAction` must be `true` or record cards won't be clickable.

### 3. Navigation Service Registration

**File:** `AgentforceClientManager.kt` (Line 181)

```kotlin
val config = AgentforceConfiguration.builder(credentialProvider)
    // ... other configuration
    .setNavigation(navigationService)  // Register navigation service
    .build()
```

### 4. Use FullConfig Mode (SDK Bug Workaround)

**File:** `AgentforceClientManager.kt` (Lines 184-186)

```kotlin
// Use FullConfig mode instead of EmployeeAgent to ensure navigation is preserved
// Note: EmployeeAgent mode has a bug where it doesn't copy the navigation setting
val agentforceMode = AgentforceMode.FullConfig(config)
```

**Why:** `AgentforceMode.EmployeeAgent` has a bug where it doesn't copy the navigation setting during configuration processing (AgentforceClient.kt lines 253-272 in SDK source).

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
```java
if (agentforceClient == null) {
    promise.reject("NOT_INITIALIZED", "AgentforceClient not initialized.");
    return;
}
```

---

## Performance Considerations

- **Bridge Crossings:** 3 total (kept to minimum)
- **Threading:** All UI work on Main thread, JS work on JS thread
- **Data Size:** Minimal (only ID and type passed across bridge initially)
- **Lookup:** O(n) contact search in state (acceptable for typical contact list sizes)

---

## Testing Checklist

- [ ] User taps contact link in Agentforce chat
- [ ] SalesforceNavigationService receives Record destination
- [ ] Event is emitted to React Native with correct data
- [ ] Contact is found in state
- [ ] Agentforce chat is dismissed successfully
- [ ] ContactDetail screen is displayed
- [ ] Back navigation returns to ContactList
- [ ] Contact not in list shows alert
- [ ] Error handling for dismiss failure works
