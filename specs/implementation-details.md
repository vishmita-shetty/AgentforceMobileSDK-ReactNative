# Architecture Specification

## Overview
Technical architecture for Service Agent integration in React Native, detailing native module implementations, threading models, and SDK integration patterns.

---

## iOS Implementation

### Module Structure

#### AgentforceModule (Swift)
**File**: `ios/ReactAgentforce/Agentforce/AgentforceModule.swift`

```swift
@objc(AgentforceModule)
class AgentforceModule: NSObject, RCTBridgeModule
```

**Responsibilities**:
- React Native bridge interface
- UI presentation/dismissal
- Method routing to ServiceAgentManager
- Main thread coordination for UI operations

**Key Methods**:
- `configure(_:organizationId:esDeveloperName:resolver:rejecter:)` - Maps JS params to native config
- `launchConversation(_:rejecter:)` - Presents chat UI modally
- `presentConversationView<Content: View>(_:)` - Generic SwiftUI presentation
- `dismissConversation()` - Modal dismissal
- `getRootViewController()` - UIViewController hierarchy navigation

**Threading Model**:
```swift
@MainActor
func launchConversation(...) {
    Task { @MainActor in
        // All UI operations on main thread
        let chatView = try client.createAgentforceChatView(...)
        presentConversationView(chatView)
    }
}
```

#### ServiceAgentManager (Swift)
**File**: `ios/ReactAgentforce/Agentforce/ServiceAgentManager.swift`

```swift
class ServiceAgentManager: ObservableObject
```

**Pattern**: Singleton with `@Published` properties for SwiftUI reactivity

**State Properties**:
```swift
@Published var orgUrl: String = "" {
    didSet { UserDefaults.standard.set(orgUrl, forKey: "ServiceAgentOrgUrl") }
}
@Published var isConfigured: Bool = false
@Published var isInitialized: Bool = false
```

**Persistence Pattern**:
- Property observers (`didSet`) auto-save to UserDefaults
- `init()` calls `loadFromUserDefaults()` on creation
- Keys: `ServiceAgentOrgUrl`, `ServiceAgentApiName`, `ServiceAgentDevName`, `ServiceAgentSiteUrl`

**SDK Lifecycle**:
```swift
func initializeSDK() throws {
    cleanupSDK()  // Clear existing client/conversation
    
    let serviceConfig = ServiceAgentConfiguration(
        esDeveloperName: devName,
        organizationId: orgUrl,
        serviceApiURL: siteUrl
    )
    
    agentforceClient = AgentforceClient(
        credentialProvider: credentialProvider,
        mode: .serviceAgent(serviceConfig)
    )
}
```

**Conversation Management**:
- `startConversation()` - Returns existing or creates new
- `getCurrentConversation()` - Returns retained conversation
- `closeConversation()` - Async cleanup with SDK call
- `cleanupSDK()` - Full reset (client, conversation, state)

#### ServiceAgentCredentialProvider (Swift)
**File**: `ios/ReactAgentforce/Agentforce/ServiceAgentCredentialProvider.swift`

```swift
class ServiceAgentCredentialProvider: AgentforceAuthCredentialProviding {
    func getAuthCredentials() -> AgentforceAuthCredentials {
        return .OAuth(
            authToken: "",
            orgId: organizationUrl,
            userId: ""
        )
    }
}
```

**Pattern**: Empty token OAuth for Service Agent mode
- No user authentication required
- Only orgId populated
- Stateless credential provider

### UI Presentation Pattern

**Modal Presentation**:
```swift
let hostingController = UIHostingController(rootView: chatView)
hostingController.modalPresentationStyle = .fullScreen
rootViewController.present(hostingController, animated: true)
```

**Dismissal**:
```swift
window.rootViewController?.dismiss(animated: true) {
    // Cleanup callback
}
```

**View Hierarchy Navigation**:
- Gets key window from `UIWindowScene`
- Returns base root view controller (not topmost)
- Allows proper modal presentation/dismissal

---

## Android Implementation

### Module Structure

#### ServiceAgentModule (Kotlin)
**File**: `android/app/src/main/java/.../ServiceAgentModule.kt`

```kotlin
class ServiceAgentModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext)
```

**Responsibilities**:
- React Native bridge interface
- ViewModel lifecycle management
- Activity launching
- Coroutine scope management

**ViewModel Management**:
```kotlin
private var viewModel: ServiceAgentViewModel? = null

// Lazy creation on first method call
if (viewModel == null && activity is ViewModelStoreOwner) {
    viewModel = ViewModelProvider(
        activity,
        ViewModelProvider.AndroidViewModelFactory.getInstance(...)
    )[ServiceAgentViewModel::class.java]
}
```

**Threading Model**:
```kotlin
@ReactMethod
fun configure(..., promise: Promise) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            viewModel?.initializeAgentforce()
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("ERROR", e.message)
        }
    }
}
```

#### ServiceAgentViewModel (Kotlin)
**File**: `android/app/src/main/java/.../ServiceAgentViewModel.kt`

```kotlin
class ServiceAgentViewModel(application: Application) : AndroidViewModel(application)
```

**Pattern**: AndroidViewModel with StateFlow for reactive state

**State Management**:
```kotlin
private val _serviceApiURL = MutableStateFlow("")
val serviceApiURL: StateFlow<String> = _serviceApiURL.asStateFlow()

private val _isConfigured = MutableStateFlow(false)
val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()
```

**Persistence Pattern**:
```kotlin
private val prefs = application.getSharedPreferences("ServiceAgentPrefs", Context.MODE_PRIVATE)

private fun saveConfiguration() {
    prefs.edit()
        .putString(KEY_SERVICE_API_URL, _serviceApiURL.value)
        .commit()  // Synchronous save
}

init {
    loadConfiguration()  // Auto-load on creation
    updateConfiguredState()
}
```

**SDK Lifecycle**:
```kotlin
fun initializeAgentforce() {
    AgentforceClientHolder.clear()  // Clear existing
    
    viewModelScope.launch {
        val serviceAgentMode = AgentforceMode.ServiceAgent(
            serviceAgentConfiguration = ServiceAgentConfiguration.builder(...)
            agentforceConfiguration = AgentforceConfiguration.builder(...)
        )
        
        val client = AgentforceClient()
        client.init(serviceAgentMode, application)
        AgentforceClientHolder.setClient(client)
    }
}
```

**Conversation Management**:
- `startConversation()` - Checks holder for existing, creates if needed
- `startNewConversation()` - Closes existing, starts fresh
- `closeConversation()` - Clears references (no explicit close in Android SDK)

#### AgentforceClientHolder (Kotlin)
**File**: `android/app/src/main/java/.../AgentforceClientHolder.kt`

```kotlin
object AgentforceClientHolder {
    @Volatile
    var agentforceClient: AgentforceClient? = null
        private set
    
    @Volatile
    var currentConversation: AgentforceConversation? = null
        private set
}
```

**Pattern**: Thread-safe singleton for cross-activity state
- `@Volatile` for visibility guarantees
- Private setters with public getters
- Survives activity recreation

### UI Presentation Pattern

**Activity Launch**:
```kotlin
val intent = Intent(activity, ServiceAgentConversationActivity::class.java)
activity.startActivity(intent)
```

**Compose UI**:
```kotlin
@Composable
fun ConversationScreen(viewModel: ServiceAgentViewModel, onClose: () -> Unit) {
    Scaffold(topBar = { TopAppBar(...) }) {
        client.AgentforceConversationContainer(
            conversation = conversation,
            onClose = onClose
        )
    }
}
```

---

## Cross-Platform Patterns

### Configuration Flow
1. JS calls `configure(serviceApiURL, organizationId, esDeveloperName)`
2. Native module creates/retrieves ViewModel/Manager
3. Configuration saved to persistent storage
4. SDK initialization triggered asynchronously
5. Success/failure returned via Promise

### Conversation Reuse Strategy
```
Launch 1: No conversation → Create new → Store reference
Launch 2: Conversation exists → Reuse stored reference
Config change: Clear references → Next launch creates new
```

### State Synchronization
- **iOS**: `@Published` properties + UserDefaults observers
- **Android**: `StateFlow` + SharedPreferences + manual sync
- Both: Auto-load on app start, auto-save on changes

### Threading Guarantees
- **iOS**: `@MainActor` for UI, `Task` for async
- **Android**: `Dispatchers.Main` for UI, `viewModelScope` for lifecycle-aware coroutines
- Both: All SDK initialization on main thread due to SDK requirements

---

## Error Handling

### iOS Error Types
```swift
enum ServiceAgentError: LocalizedError {
    case notConfigured
    case sdkNotInitialized
    case failedToStartConversation
    case noActiveConversation
    case invalidConfiguration
}
```

### Android Error Handling
```kotlin
try {
    viewModel?.initializeAgentforce()
    promise.resolve(true)
} catch (e: Exception) {
    Log.e(TAG, "Failed to initialize", e)
    promise.reject("ERROR", "Failed to initialize: ${e.message}")
}
```

### JS Promise Pattern
```typescript
try {
    await AgentforceModule.configure(...)
} catch (error) {
    // Native error propagated to JS
}
```

---

## Memory Management

### iOS
- ServiceAgentManager: Singleton, lives for app lifetime
- AgentforceClient: Strong reference in manager, cleared on `cleanupSDK()`
- Conversation: Strong reference, cleared on close or cleanup
- UIHostingController: Released when dismissed

### Android
- ServiceAgentViewModel: Lifecycle-aware, survives configuration changes
- AgentforceClientHolder: Singleton, manual clear required
- Conversation: Weak-ish reference via holder
- Activity: Standard lifecycle, recreated on rotation

### Cleanup Triggers
- **Configuration change**: Both platforms clear client/conversation
- **App termination**: iOS releases all, Android holder persists
- **Explicit reset**: `resetSettings()` clears everything

