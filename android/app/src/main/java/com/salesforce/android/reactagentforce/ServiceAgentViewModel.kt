/*
 * Copyright (c) 2024-present, salesforce.com, inc.
 * All rights reserved.
 */
package com.salesforce.android.reactagentforce

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.salesforce.android.agentforcesdkimpl.AgentforceClient
import com.salesforce.android.agentforcesdkimpl.AgentforceConversation
import com.salesforce.android.agentforcesdkimpl.configuration.AgentforceConfiguration
import com.salesforce.android.agentforcesdkimpl.configuration.AgentforceMode
import com.salesforce.android.agentforcesdkimpl.configuration.ServiceAgentConfiguration
import com.salesforce.android.agentforceservice.AgentforceAuthCredentialProvider
import com.salesforce.android.agentforceservice.AgentforceAuthCredentials
import com.salesforce.android.mobile.interfaces.user.Org
import com.salesforce.android.mobile.interfaces.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Agentforce Service Agent SDK lifecycle and state
 */
class ServiceAgentViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ServiceAgentViewModel"
    }

    // Service Agent configuration
    private val _serviceApiURL = MutableStateFlow("")
    val serviceApiURL: StateFlow<String> = _serviceApiURL.asStateFlow()

    private val _organizationId = MutableStateFlow("")
    val organizationId: StateFlow<String> = _organizationId.asStateFlow()

    private val _esDeveloperName = MutableStateFlow("")
    val esDeveloperName: StateFlow<String> = _esDeveloperName.asStateFlow()

    // SDK state - using shared holder to persist across activities
    val agentforceClient: AgentforceClient? get() = AgentforceClientHolder.agentforceClient

    private val _conversation = MutableStateFlow<AgentforceConversation?>(null)
    val conversation: StateFlow<AgentforceConversation?> = _conversation.asStateFlow()

    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()
    
    init {
        // Sync state with holder
        _isConfigured.value = AgentforceClientHolder.isConfigured
        _conversation.value = AgentforceClientHolder.currentConversation
    }

    /**
     * Update Service Agent configuration
     */
    fun updateConfiguration(
        serviceApiURL: String,
        organizationId: String,
        esDeveloperName: String
    ) {
        Log.d(TAG, "Updating configuration")
        _serviceApiURL.value = serviceApiURL
        _organizationId.value = organizationId
        _esDeveloperName.value = esDeveloperName
    }

    /**
     * Initialize Agentforce SDK with Service Agent mode
     */
    fun initializeAgentforce() {
        Log.d(TAG, "initializeAgentforce() called")

        if (_serviceApiURL.value.isEmpty() ||
            _organizationId.value.isEmpty() ||
            _esDeveloperName.value.isEmpty()
        ) {
            Log.e(TAG, "Configuration incomplete")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Creating Service Agent configuration")

                // Simple auth provider for Service Agent (no authentication needed)
                val authCredentialProvider = object : AgentforceAuthCredentialProvider {
                    override fun getAuthCredentials(): AgentforceAuthCredentials {
                        return AgentforceAuthCredentials.OAuth(
                            authToken = "", // Service agent doesn't need token
                            orgId = _organizationId.value,
                            userId = "" // Service agent doesn't need userId
                        )
                    }
                }

                // Service Agent mode configuration
                val serviceAgentMode = AgentforceMode.ServiceAgent(
                    serviceAgentConfiguration = ServiceAgentConfiguration
                        .builder(
                            esDeveloperName = _esDeveloperName.value,
                            organizationId = _organizationId.value,
                            serviceApiURL = _serviceApiURL.value
                        )
                        .build(),
                    agentforceConfiguration = AgentforceConfiguration
                        .builder(authCredentialProvider)
                        .setServiceApiURL(_serviceApiURL.value)
                        .setSalesforceDomain(_serviceApiURL.value)
                        .setUser(
                            User(
                                org = Org(id = _organizationId.value, community = null),
                                userName = "service_user",
                                displayName = "Service User"
                            )
                        )
                        .setApplication(getApplication())
                        .build()
                )

                Log.d(TAG, "Initializing AgentforceClient")

                // Initialize client
                val client = AgentforceClient()
                client.init(
                    agentforceMode = serviceAgentMode,
                    application = getApplication()
                )

                // Store in shared holder
                AgentforceClientHolder.setClient(client)
                _isConfigured.value = true

                Log.d(TAG, "Agentforce initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Agentforce", e)
                _isConfigured.value = false
                throw e
            }
        }
    }

    /**
     * Start a new conversation
     */
    fun startConversation() {
        Log.d(TAG, "startConversation() called")
        val client = AgentforceClientHolder.agentforceClient
        if (client == null) {
            Log.e(TAG, "AgentforceClient is null - not initialized")
            return
        }
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting conversation with client: $client")
                val newConversation = client.startAgentforceConversation()
                _conversation.value = newConversation
                AgentforceClientHolder.setConversation(newConversation)
                Log.d(TAG, "Conversation started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start conversation", e)
            }
        }
    }

    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
        // Don't clear the shared client holder - it should persist
        _conversation.value = null
    }
}

