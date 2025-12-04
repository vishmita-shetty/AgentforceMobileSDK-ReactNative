# API Specification

## JavaScript/TypeScript Interface

### AgentforceService Class

**File**: `src/services/AgentforceService.ts`

---

#### `configure(config: ServiceAgentConfig): Promise<boolean>`

Configures and initializes the Service Agent SDK with provided credentials.

**Parameters**:
```typescript
interface ServiceAgentConfig {
  serviceApiURL: string;      // Salesforce instance URL (e.g., "https://example.my.salesforce.com")
  organizationId: string;     // Organization ID or URL
  esDeveloperName: string;    // Einstein Service developer name
}
```

**Returns**: `Promise<boolean>`
- `true` - Configuration saved and SDK initialized successfully
- Throws error on failure

**Behavior**:
- Validates all fields are non-empty
- Saves configuration to persistent storage (UserDefaults/SharedPreferences)
- Initializes AgentforceClient with Service Agent mode
- Clears any existing conversation/client
- Auto-initializes on successful configuration

**Errors**:
```typescript
"All configuration parameters are required"  // Empty field
"Failed to initialize: {detail}"            // SDK initialization failure
"Activity not available"                    // Android: No current activity
```

**Example**:
```typescript
try {
  await AgentforceService.configure({
    serviceApiURL: "https://example.my.salesforce.com",
    organizationId: "00D...",
    esDeveloperName: "MyServiceAgent"
  });
  console.log("Configured successfully");
} catch (error) {
  console.error("Configuration failed:", error);
}
```

---

#### `launchConversation(): Promise<boolean>`

Launches the Agentforce chat UI. Reuses existing conversation if available, otherwise creates new one.

**Parameters**: None

**Returns**: `Promise<boolean>`
- `true` - Conversation launched successfully
- Throws error on failure

**Behavior**:
- Checks if SDK is configured
- If SDK not initialized but configured, initializes automatically (Android)
- Reuses `currentConversation` if exists
- Creates new conversation if none exists
- Presents modal UI (iOS) or launches activity (Android)

**Conversation Reuse Logic**:
```
If currentConversation exists → Reuse
Else if client initialized → Create new conversation
Else if configured → Initialize SDK → Create conversation
Else → Throw error
```

**Errors**:
```typescript
"SDK not configured. Call configure() first."
"Failed to launch: {detail}"
"Activity not available"  // Android only
```

**Example**:
```typescript
try {
  await AgentforceService.launchConversation();
} catch (error) {
  if (error.message.includes("not configured")) {
    // Redirect to settings
  }
}
```

---

#### `startNewConversation(): Promise<boolean>`

Forces a new conversation, closing any existing one.

**Parameters**: None

**Returns**: `Promise<boolean>`
- `true` - New conversation started successfully
- Throws error on failure

**Behavior**:
- Closes existing conversation
- Clears conversation reference
- Creates fresh conversation
- Launches UI with new conversation

**Use Cases**:
- User wants to start over
- Switching between different topics
- Testing different conversation flows

**Errors**: Same as `launchConversation()`

**Example**:
```typescript
// User clicks "New Conversation" button
await AgentforceService.startNewConversation();
```

---

#### `isConfigured(): Promise<boolean>`

Checks if SDK has valid saved configuration.

**Parameters**: None

**Returns**: `Promise<boolean>`
- `true` - Valid configuration exists (all 3 fields populated)
- `false` - No configuration or incomplete configuration

**Behavior**:
- Creates ViewModel/Manager if needed (to load from storage)
- Checks `isConfigured` state flag
- Does NOT check if SDK is initialized

**Example**:
```typescript
const configured = await AgentforceService.isConfigured();
if (!configured) {
  navigation.navigate('Settings');
}
```

---

#### `getConfiguration(): Promise<ServiceAgentConfig | null>`

Retrieves saved configuration from persistent storage.

**Parameters**: None

**Returns**: `Promise<ServiceAgentConfig | null>`
```typescript
{
  serviceApiURL: string,
  organizationId: string,
  esDeveloperName: string
} | null  // null if no configuration saved
```

**Behavior**:
- Loads from UserDefaults (iOS) or SharedPreferences (Android)
- Returns `null` if all fields are empty
- Creates ViewModel/Manager if needed to access storage

**Example**:
```typescript
const config = await AgentforceService.getConfiguration();
if (config) {
  setServiceApiURL(config.serviceApiURL);
  setOrganizationId(config.organizationId);
  setEsDeveloperName(config.esDeveloperName);
}
```

---

## iOS Native Bridge

### AgentforceModule.swift

All methods exposed via `RCT_EXTERN_METHOD` in `AgentforceModule.m`

---

#### `configure(_:organizationId:esDeveloperName:resolver:rejecter:)`

```swift
@objc
func configure(
    _ serviceApiURL: String,
    organizationId: String,
    esDeveloperName: String,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
)
```

**Implementation**:
```swift
Task { @MainActor in
    do {
        ServiceAgentManager.shared.configure(
            orgUrl: organizationId,
            apiName: esDeveloperName,
            devName: esDeveloperName,
            siteUrl: serviceApiURL
        )
        try ServiceAgentManager.shared.initializeSDK()
        resolve(true)
    } catch {
        reject("CONFIG_ERROR", error.localizedDescription, error)
    }
}
```

---

#### `launchConversation(_:rejecter:)`

```swift
@objc
func launchConversation(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
)
```

**Implementation**:
```swift
Task { @MainActor in
    let client = ServiceAgentManager.shared.getClient()
    let conversation = ServiceAgentManager.shared.startConversation()
    
    let chatView = try client.createAgentforceChatView(
        conversation: conversation,
        delegate: nil,
        showTopBar: true,
        onContainerClose: {
            Task { @MainActor in
                self.dismissConversation()
            }
        }
    )
    
    presentConversationView(chatView)
    resolve(["success": true])
}
```

---

#### `getConfiguration(_:rejecter:)`

```swift
@objc
func getConfiguration(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
)
```

**Returns**:
```swift
[
    "serviceApiURL": String,
    "organizationId": String,
    "esDeveloperName": String
]
```

---

#### `isConfigured(_:rejecter:)`

```swift
@objc
func isConfigured(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
)
```

**Returns**: `Bool` - Result of `ServiceAgentManager.shared.isConfigured`

---

#### `isInitialized(_:rejecter:)`

```swift
@objc
func isInitialized(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
)
```

**Returns**: `Bool` - Result of `ServiceAgentManager.shared.isInitialized`

---

#### `closeConversation(_:rejecter:)`

```swift
@objc
func closeConversation(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
)
```

**Behavior**:
- Calls `await conversation.closeConversation()`
- Dismisses modal UI
- Clears conversation reference

---

#### `startNewConversation(_:rejecter:)`

```swift
@objc
func startNewConversation(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
)
```

**Behavior**: Same as `launchConversation` but forces new conversation via `startNewConversation()`

---

#### `resetSettings(_:rejecter:)`

```swift
@objc
func resetSettings(
    _ resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
)
```

**Behavior**:
- Calls `ServiceAgentManager.shared.resetToDefaults()`
- Clears UserDefaults
- Resets all state flags
- Calls `cleanupSDK()`

---

## Android Native Bridge

### ServiceAgentModule.kt

All methods exposed via `@ReactMethod` annotation

---

#### `configure(serviceApiURL, organizationId, esDeveloperName, promise)`

```kotlin
@ReactMethod
fun configure(
    serviceApiURL: String,
    organizationId: String,
    esDeveloperName: String,
    promise: Promise
)
```

**Implementation**:
```kotlin
CoroutineScope(Dispatchers.Main).launch {
    try {
        viewModel?.updateConfiguration(serviceApiURL, organizationId, esDeveloperName)
        viewModel?.initializeAgentforce()
        promise.resolve(true)
    } catch (e: Exception) {
        promise.reject("ERROR", "Failed to initialize: ${e.message}")
    }
}
```

**Threading**: Main dispatcher for SDK requirements

---

#### `launchConversation(promise)`

```kotlin
@ReactMethod
fun launchConversation(promise: Promise)
```

**Implementation**:
```kotlin
if (!AgentforceClientHolder.isConfigured) {
    CoroutineScope(Dispatchers.Main).launch {
        viewModel?.initializeAgentforce()
        delay(500)  // Wait for initialization
        launchActivity()
    }
} else {
    launchActivity()
}

private fun launchActivity() {
    val intent = Intent(activity, ServiceAgentConversationActivity::class.java)
    activity.startActivity(intent)
    promise.resolve(true)
}
```

**Auto-Initialization**: If configured but not initialized, initializes before launching

---

#### `getConfiguration(promise)`

```kotlin
@ReactMethod
fun getConfiguration(promise: Promise)
```

**Returns**:
```kotlin
Arguments.createMap().apply {
    putString("serviceApiURL", config["serviceApiURL"])
    putString("organizationId", config["organizationId"])
    putString("esDeveloperName", config["esDeveloperName"])
}
```

**ViewModel Creation**: Automatically creates ViewModel if needed to access SharedPreferences

---

#### `isConfigured(promise)`

```kotlin
@ReactMethod
fun isConfigured(promise: Promise)
```

**Returns**: `Boolean` from `viewModel?.isConfigured?.value ?: false`

**Logic**: Checks if all three config fields are non-blank

---

#### `isInitialized(promise)`

```kotlin
@ReactMethod
fun isInitialized(promise: Promise)
```

**Returns**: `Boolean` from `AgentforceClientHolder.isConfigured`

**Difference from `isConfigured`**: Checks if SDK is actually initialized, not just configured

---

#### `closeConversation(promise)`

```kotlin
@ReactMethod
fun closeConversation(promise: Promise)
```

**Behavior**:
- Calls `viewModel?.closeConversation()`
- Clears conversation reference in holder
- Does NOT call SDK close (not available in Android SDK)

---

#### `startNewConversation(promise)`

```kotlin
@ReactMethod
fun startNewConversation(promise: Promise)
```

**Implementation**:
```kotlin
viewModel?.startNewConversation()  // Closes existing, starts fresh
val intent = Intent(activity, ServiceAgentConversationActivity::class.java)
activity.startActivity(intent)
```

---

#### `resetSettings(promise)`

```kotlin
@ReactMethod
fun resetSettings(promise: Promise)
```

**Behavior**:
- Calls `viewModel?.resetConfiguration()`
- Clears SharedPreferences
- Calls `AgentforceClientHolder.clear()`

---

## Error Code Reference

### Common Errors

| Code | Description | Platform | Resolution |
|------|-------------|----------|------------|
| `CONFIG_ERROR` | Invalid configuration | iOS | Check all fields populated, valid URLs |
| `ERROR` | Generic error | Android | Check error message for details |
| `LAUNCH_ERROR` | Failed to launch conversation | iOS | Ensure SDK initialized |
| `START_NEW_ERROR` | Failed to start new conversation | iOS | Check SDK state |
| `Activity not available` | No current activity | Android | Retry when app in foreground |
| `SDK not configured` | Configure not called | Both | Call `configure()` first |
| `SDK not initialized` | Client not created | Both | Wait for `configure()` to complete |

---

## Platform Differences

| Feature | iOS | Android |
|---------|-----|---------|
| **Conversation Close** | Explicit `closeConversation()` call | Reference clearing only |
| **UI Presentation** | Modal UIHostingController | New Activity |
| **Auto-Initialize on Launch** | No | Yes (if configured) |
| **ViewModel Lifecycle** | Singleton for app lifetime | Activity-scoped, recreated |
| **Threading** | `@MainActor` + `Task` | Coroutines + `Dispatchers.Main` |
| **Error Format** | `LocalizedError` description | Exception message string |

---

## Testing Checklist

### Configuration
- [ ] Configure with valid credentials → Success
- [ ] Configure with empty field → Error
- [ ] Configure twice → Second replaces first
- [ ] App restart → Configuration persists

### Launching
- [ ] Launch before configure → Error
- [ ] Launch after configure → Opens UI
- [ ] Launch twice → Reuses conversation
- [ ] Config change → New conversation on launch

### Persistence
- [ ] `isConfigured()` after restart → `true`
- [ ] `getConfiguration()` after restart → Returns saved config
- [ ] Settings screen → Fields pre-filled

### Conversation Lifecycle
- [ ] Launch → Send message → Close → Launch → Same conversation
- [ ] Change config → Launch → New conversation
- [ ] `startNewConversation()` → Fresh conversation
- [ ] `resetSettings()` → All cleared

