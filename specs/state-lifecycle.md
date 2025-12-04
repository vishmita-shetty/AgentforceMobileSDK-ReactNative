# State Management Specification

## Configuration State Machine

### States

```
UNCONFIGURED → CONFIGURED → INITIALIZED → CONVERSATION_ACTIVE
     ↓            ↓              ↓               ↓
   RESET ←────────┴──────────────┴───────────────┘
```

#### UNCONFIGURED
**Conditions**:
- App first launch
- After `resetSettings()` called
- All config fields empty in storage

**Available Actions**:
- `configure()` → CONFIGURED

**UI State**:
- `isConfigured() → false`
- Home screen shows "Configuration Required"
- Launch button disabled or shows error

---

#### CONFIGURED
**Conditions**:
- `configure()` successfully saved to storage
- All three fields (serviceApiURL, organizationId, esDeveloperName) non-empty
- May or may not have initialized SDK

**Available Actions**:
- `launchConversation()` → INITIALIZED (if needed) → CONVERSATION_ACTIVE
- `configure()` again → CONFIGURED (reinitializes)
- `resetSettings()` → UNCONFIGURED

**State Flags**:
- iOS: `ServiceAgentManager.shared.isConfigured = true`
- Android: `viewModel.isConfigured.value = true`

**Persistence**:
- iOS: UserDefaults keys populated
- Android: SharedPreferences keys populated

**UI State**:
- `isConfigured() → true`
- Home screen shows "✓ Ready to launch"
- Launch button enabled

---

#### INITIALIZED
**Conditions**:
- `AgentforceClient` successfully created
- Credential provider configured
- Service Agent mode set

**State Flags**:
- iOS: `ServiceAgentManager.shared.isInitialized = true`
- iOS: `ServiceAgentManager.shared.agentforceClient != nil`
- Android: `AgentforceClientHolder.isConfigured = true`
- Android: `AgentforceClientHolder.agentforceClient != null`

**Transition Triggers**:
- Automatic: On successful `configure()`
- Automatic: On `launchConversation()` if configured but not initialized (Android only)

**Available Actions**:
- `launchConversation()` → CONVERSATION_ACTIVE
- `configure()` again → Re-initializes (clears client) → INITIALIZED
- `resetSettings()` → UNCONFIGURED

---

#### CONVERSATION_ACTIVE
**Conditions**:
- Conversation created via `client.startAgentforceConversation()`
- Conversation reference retained

**State Flags**:
- iOS: `ServiceAgentManager.shared.currentConversation != nil`
- Android: `AgentforceClientHolder.currentConversation != null`

**Available Actions**:
- `launchConversation()` → Reuses conversation (stays in CONVERSATION_ACTIVE)
- `startNewConversation()` → Closes old, creates new (stays in CONVERSATION_ACTIVE)
- `closeConversation()` → INITIALIZED
- `configure()` → Clears conversation → INITIALIZED
- Close UI button → CONVERSATION_ACTIVE (conversation retained)

**Persistence Across**:
- ✅ UI dismissal and reopening
- ✅ Activity recreation (Android)
- ❌ App termination
- ❌ Configuration change

---

## Configuration Persistence

### iOS Implementation

#### Storage Keys
```swift
"ServiceAgentOrgUrl"      → organizationId
"ServiceAgentApiName"     → esDeveloperName (legacy key name)
"ServiceAgentDevName"     → esDeveloperName
"ServiceAgentSiteUrl"     → serviceApiURL
```

#### Write Pattern
```swift
@Published var orgUrl: String = "" {
    didSet {
        UserDefaults.standard.set(orgUrl, forKey: "ServiceAgentOrgUrl")
    }
}
```

**Characteristics**:
- Automatic on property change
- Synchronous write
- No explicit save call needed

#### Read Pattern
```swift
private func loadFromUserDefaults() {
    orgUrl = UserDefaults.standard.string(forKey: "ServiceAgentOrgUrl") ?? ""
    apiName = UserDefaults.standard.string(forKey: "ServiceAgentApiName") ?? ""
    devName = UserDefaults.standard.string(forKey: "ServiceAgentDevName") ?? ""
    siteUrl = UserDefaults.standard.string(forKey: "ServiceAgentSiteUrl") ?? ""
    
    credentialProvider.updateConfiguration(organizationUrl: orgUrl)
    validateConfiguration()
}
```

**Trigger**: `init()` of ServiceAgentManager singleton

---

### Android Implementation

#### Storage Keys
```kotlin
"serviceApiURL"      → serviceApiURL
"organizationId"     → organizationId
"esDeveloperName"    → esDeveloperName
```

#### Write Pattern
```kotlin
private fun saveConfiguration() {
    prefs.edit()
        .putString(KEY_SERVICE_API_URL, _serviceApiURL.value)
        .putString(KEY_ORGANIZATION_ID, _organizationId.value)
        .putString(KEY_ES_DEVELOPER_NAME, _esDeveloperName.value)
        .commit()  // Synchronous
}
```

**Characteristics**:
- Manual call in `updateConfiguration()`
- Synchronous (`commit()` not `apply()`)
- Builder pattern for multiple puts

#### Read Pattern
```kotlin
init {
    loadConfiguration()
    updateConfiguredState()
    _conversation.value = AgentforceClientHolder.currentConversation
}

private fun loadConfiguration() {
    _serviceApiURL.value = prefs.getString(KEY_SERVICE_API_URL, "") ?: ""
    _organizationId.value = prefs.getString(KEY_ORGANIZATION_ID, "") ?: ""
    _esDeveloperName.value = prefs.getString(KEY_ES_DEVELOPER_NAME, "") ?: ""
}
```

**Trigger**: `init` block of ViewModel

---

## Conversation Lifecycle

### Creation Flow

#### iOS
```swift
func startConversation() -> AgentConversation {
    // Reuse if exists
    if let existingConversation = currentConversation {
        return existingConversation
    }
    
    // Create new
    let conversation = client.startAgentforceConversation(
        forESDeveloperName: devName
    )
    currentConversation = conversation
    return conversation
}
```

#### Android
```kotlin
fun startConversation() {
    // Reuse if exists
    val existingConversation = AgentforceClientHolder.currentConversation
    if (existingConversation != null) {
        _conversation.value = existingConversation
        return
    }
    
    // Create new
    viewModelScope.launch {
        val newConversation = client.startAgentforceConversation()
        _conversation.value = newConversation
        AgentforceClientHolder.setConversation(newConversation)
    }
}
```

**Key Difference**: iOS synchronous, Android coroutine-based

---

### Retention Strategy

#### iOS
**Storage**: Strong reference in `ServiceAgentManager.shared.currentConversation`

**Lifetime**: 
- Created on first launch
- Survives app backgrounding
- Survives ViewController dismissal
- Cleared on:
  - Explicit `closeConversation()` call
  - `cleanupSDK()` call (from `initializeSDK()` or `resetToDefaults()`)
  - App termination

**Access Pattern**: Direct property access on singleton

---

#### Android
**Storage**: Reference in both ViewModel and AgentforceClientHolder

**Lifetime**:
- Created on first launch
- Survives activity recreation
- Survives process death (via holder)
- Cleared on:
  - Explicit `closeConversation()` call
  - `AgentforceClientHolder.clear()` call
  - New configuration
  - `resetConfiguration()`

**Access Pattern**: 
```kotlin
val conversation = AgentforceClientHolder.currentConversation
    ?: viewModel.conversation.value
```

---

### Closure Flow

#### iOS
```swift
func closeConversation() async {
    if let conversation = currentConversation {
        try await conversation.closeConversation()  // SDK call
    }
    currentConversation = nil
}
```

**Characteristics**:
- Explicit SDK call to close
- Async operation
- Reference cleared after close

---

#### Android
```kotlin
fun closeConversation() {
    _conversation.value = null
    AgentforceClientHolder.setConversation(null)
}
```

**Characteristics**:
- No SDK close call (not available)
- Reference clearing only
- Synchronous operation

---

## State Synchronization

### Cross-Launch Persistence

#### Scenario: App Launch → Configure → Close → Relaunch

**iOS Flow**:
```
Launch 1:
  ServiceAgentManager.init() →
  loadFromUserDefaults() → [empty] →
  isConfigured = false

Configure:
  configure(...) → 
  Properties set → didSet triggers →
  UserDefaults.set(...) →
  isConfigured = true

Close App & Relaunch:
  ServiceAgentManager.init() →
  loadFromUserDefaults() → [populated] →
  isConfigured = true
```

**Android Flow**:
```
Launch 1:
  ViewModel created →
  loadConfiguration() → [empty] →
  updateConfiguredState() → isConfigured = false

Configure:
  updateConfiguration(...) →
  saveConfiguration() →
  prefs.edit().commit() →
  updateConfiguredState() → isConfigured = true

Close App & Relaunch:
  ViewModel created →
  loadConfiguration() → [populated] →
  updateConfiguredState() → isConfigured = true
```

---

### Configuration Change Impact

#### Scenario: Config A → Launch → Change to Config B → Launch

**Expected Behavior**:
```
1. Configure with A → SDK initialized with A
2. Launch → Conversation with A created
3. Configure with B → SDK cleared and reinitialized with B
4. Launch → New conversation with B (old conversation cleared)
```

**Implementation**:

iOS:
```swift
func initializeSDK() throws {
    cleanupSDK()  // ← Clears client and conversation
    // ... create new client with new config
}
```

Android:
```kotlin
fun initializeAgentforce() {
    AgentforceClientHolder.clear()  // ← Clears client
    _conversation.value = null       // ← Clears conversation
    // ... create new client with new config
}
```

---

## State Queries

### isConfigured vs isInitialized

#### `isConfigured()`
**Checks**: Configuration exists in storage (all 3 fields non-empty)

**iOS**:
```swift
var isConfigured: Bool {
    !orgUrl.isEmpty && !apiName.isEmpty && 
    !devName.isEmpty && !siteUrl.isEmpty
}
```

**Android**:
```kotlin
private fun updateConfiguredState() {
    _isConfigured.value = 
        _serviceApiURL.value.isNotBlank() &&
        _organizationId.value.isNotBlank() &&
        _esDeveloperName.value.isNotBlank()
}
```

**Returns `true`**: After `configure()` succeeds, persists across restarts

---

#### `isInitialized()`
**Checks**: SDK (AgentforceClient) has been created

**iOS**:
```swift
var isInitialized: Bool {
    agentforceClient != nil
}
```

**Android**:
```kotlin
fun isInitialized() = AgentforceClientHolder.isConfigured
```

**Returns `true`**: After SDK initialization, cleared on config change or reset

**Important**: `isConfigured` can be true while `isInitialized` is false (e.g., after app restart before first launch)

---

## Threading & Concurrency

### iOS

#### Main Actor Isolation
```swift
@MainActor
func launchConversation(...) {
    Task { @MainActor in
        // All operations guaranteed on main thread
        let chatView = try client.createAgentforceChatView(...)
        presentConversationView(chatView)
    }
}
```

**Why**: 
- UI operations require main thread
- AgentforceSDK initialization requires main thread
- SwiftUI view creation requires main thread

#### Async Operations
```swift
func closeConversation() async {
    if let conversation = currentConversation {
        try await conversation.closeConversation()  // SDK async call
    }
    currentConversation = nil
}
```

**Pattern**: Structured concurrency with async/await

---

### Android

#### Main Dispatcher
```kotlin
CoroutineScope(Dispatchers.Main).launch {
    viewModel?.initializeAgentforce()
    delay(500)  // Wait for init
    launchActivity()
}
```

**Why**:
- SDK requires main thread
- UI operations require main thread
- Activity launch requires main thread

#### ViewModel Scope
```kotlin
viewModelScope.launch {
    try {
        val newConversation = client.startAgentforceConversation()
        _conversation.value = newConversation
    } catch (e: Exception) {
        // Error handling
    }
}
```

**Characteristics**:
- Lifecycle-aware (cancelled when ViewModel cleared)
- Defaults to Dispatchers.Main
- Exception handling via try-catch

---

## Edge Cases & Race Conditions

### Rapid Configuration Changes
**Scenario**: User calls `configure()` twice in quick succession

**Mitigation**:
- iOS: Synchronous `cleanupSDK()` before creating new client
- Android: `AgentforceClientHolder.clear()` before creating new client

**Result**: Last configuration wins, no leaked clients

---

### Launch During Initialization
**Scenario**: User taps launch button while SDK is initializing

**iOS Behavior**: 
- `initializeSDK()` is synchronous
- UI disabled during Promise pending
- No race condition

**Android Behavior**:
- `initializeAgentforce()` is async (coroutine)
- `launchConversation()` checks `isConfigured` in holder
- If not initialized, waits 500ms then launches
- Possible race if init takes >500ms

**Improvement Opportunity**: Use callback/Flow for init completion

---

### Configuration Change During Active Conversation
**Scenario**: Conversation open, user navigates to settings and reconfigures

**Current Behavior**:
- Configuration saved
- SDK reinitialized (clears client/conversation)
- Open conversation UI still showing old conversation
- Old conversation reference cleared from manager/holder
- UI may show error or empty state

**Recommendation**: Dismiss conversation UI before allowing reconfiguration

---

## State Diagram

```
┌─────────────────┐
│  UNCONFIGURED   │
└────────┬────────┘
         │ configure()
         ▼
┌─────────────────┐
│   CONFIGURED    │◄──────────┐
└────────┬────────┘           │
         │ launchConversation()│
         │ (initializes if needed)
         ▼                     │
┌─────────────────┐           │
│  INITIALIZED    │           │
└────────┬────────┘           │
         │ startConversation()│
         ▼                     │
┌─────────────────┐           │
│ CONVERSATION    │           │
│    ACTIVE       │           │
└────────┬────────┘           │
         │                     │
         ├─ launchConversation() → reuses conversation
         ├─ closeConversation() → back to INITIALIZED
         ├─ configure() ────────┘ (clears conversation)
         └─ resetSettings() → back to UNCONFIGURED
```

---

## Validation Rules

### Configuration Validation
```typescript
serviceApiURL: {
  required: true,
  format: URL,
  example: "https://example.my.salesforce.com"
}

organizationId: {
  required: true,
  format: string,
  min_length: 1
}

esDeveloperName: {
  required: true,
  format: string,
  pattern: /^[a-zA-Z0-9_]+$/  // Recommended
}
```

### State Validation
- `launchConversation()` requires `isConfigured == true`
- `startConversation()` requires `agentforceClient != null`
- `closeConversation()` no-ops if `currentConversation == null`

