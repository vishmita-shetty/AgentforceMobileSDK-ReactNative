/*
 * Copyright (c) 2020-present, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.android.reactagentforce
import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.salesforce.android.agentforcesdkimpl.AgentforceClient
import com.salesforce.android.agentforcesdkimpl.configuration.AgentforceConfiguration
import com.salesforce.android.agentforcesdkimpl.configuration.AgentforceMode
import com.salesforce.android.agentforcesdkimpl.configuration.EmployeeAgentConfiguration
import com.salesforce.android.agentforcesdkimpl.utils.AgentforceFeatureFlagSettings
import com.salesforce.android.agentforceservice.conversationservice.data.AgentforcePageContext
import com.salesforce.android.mobile.interfaces.user.Community
import com.salesforce.android.mobile.interfaces.user.Org
import com.salesforce.android.mobile.interfaces.user.User
import com.salesforce.androidsdk.accounts.UserAccount
import com.salesforce.androidsdk.accounts.UserAccountManager
import com.salesforce.androidsdk.app.SalesforceSDKManager

/**
 * Manager class for Agentforce SDK integration.
 *
 * This class handles initialization of the Agentforce SDK and presentation
 * of the chat interface within a React Native application.
 */
class AgentforceClientManager(
    private val context: Context,
    private val eventEmitter: AgentforceManagerModule? = null
) {

    companion object {
        private const val TAG = "AgentforceClientManager"
    }

    private var agentforceClient: AgentforceClient? = null
    private var uiCoordinator: AgentforceUICoordinator? = null
    private var navigationService: SalesforceNavigationService? = null

    /**
     * Callback interface for initialization operations
     */
    interface InitializationCallback {
        fun onSuccess()
        fun onError(error: Exception)
    }

    /**
     * Callback interface for presentation operations
     */
    interface PresentationCallback {
        fun onSuccess()
        fun onError(error: Exception)
    }

    /**
     * Callback interface for dismissal operations
     */
    interface DismissCallback {
        fun onSuccess()
        fun onError(error: Exception)
    }

    /**
     * Initializes the Agentforce SDK with the provided configuration.
     *
     * @param agents Array of agent configurations (id, label, isDefault)
     * @param orgId The Salesforce organization ID
     * @param endpoint The Salesforce endpoint URL
     * @param callback Callback for initialization result
     */
    fun initialize(
        agents: ReadableArray,
        orgId: String,
        endpoint: String,
        activity: Activity?,
        callback: InitializationCallback
    ) {
        try {
            // Get current user account
            val userAccountManager = UserAccountManager.getInstance()
            val currentUser: UserAccount = userAccountManager.currentUser
                ?: throw IllegalStateException("No authenticated user found")

            // Extract default agent ID from agents array
            var defaultAgentId: String? = null
            for (i in 0 until agents.size()) {
                val agent = agents.getMap(i)
                if (agent.getBoolean("isDefault")) {
                    defaultAgentId = agent.getString("id")
                    break
                }
            }

            // If no default found, use first agent
            if (defaultAgentId == null && agents.size() > 0) {
                defaultAgentId = agents.getMap(0).getString("id")
            }

            if (defaultAgentId == null) {
                throw IllegalStateException("No valid agent found")
            }

            // Create protocol implementations
            val credentialProvider = AgentforceCredentialProvider()
            val networkProvider = SalesforceNetworkProvider(SalesforceSDKManager.getInstance().clientManager.peekRestClient())
            val logger = SalesforceLoggerService()

            // Create navigation service with callback to event emitter
            navigationService = SalesforceNavigationService { recordId, recordType ->
                eventEmitter?.emitNavigationEvent(recordId, recordType ?: "")
            }

            // Create feature flag settings
            val featureFlagSettings = AgentforceFeatureFlagSettings.builder()
                .enableMultiAgent(true)
                .enableMultiModalInput(true)
                .enablePDFUpload(true)
                .enableLongPauseSpeechTranscription(true)
                .setupFlags(mapOf(
                    "enableLightningTypeStreaming" to true,
                    "enableNavAndQuickFollowUpAction" to true  // Enable record navigation
                ))
                .enableTheming(true)
                .enableOnboarding(false)
                .build()

            // Get the current Salesforce instance URL
            val instanceUrl = currentUser.instanceServer

            // Create user configuration
            val user = User(
                userId = currentUser.userId ?: "",
                org = Org(
                    id = currentUser.orgId ?: "",
                    community = Community(id = currentUser.communityId ?: "")
                )
            )

            // Get the Permissions
            val permissions = activity?.let { AgentforceClientPermissions(it) }
            val app = context.applicationContext as Application

            // Create Agentforce configuration
            val config = AgentforceConfiguration.builder(credentialProvider)
                .setApplication(app)
                .setUser(user)
                .setSalesforceDomain(instanceUrl)
                .setAgentId(defaultAgentId)
                .setFeatureFlagSettings(featureFlagSettings)
                .setNetwork(networkProvider)
                .setLogger(logger)
                .setPermission(permissions)
                .setCameraUriProvider(AgentforceClientCameraUriProvider(app))
                .setDataProvider(AgentforceClientDataProvider(networkProvider))
                .setNavigation(navigationService)
                .build()

            // Use FullConfig mode instead of EmployeeAgent to ensure navigation is preserved
            // Note: EmployeeAgent mode has a bug where it doesn't copy the navigation setting
            val agentforceMode = AgentforceMode.FullConfig(config)

            // Create client
            agentforceClient = AgentforceClient()
            agentforceClient?.init(
                authCredentialProvider = credentialProvider,
                agentforceMode = agentforceMode,
                application = app
            )

            callback.onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Agentforce", e)
            callback.onError(e)
        }
    }

    /**
     * Presents the Agentforce chat view for the specified agent with optional context.
     *
     * @param agentId The agent identifier to start a conversation with (empty for agent picker)
     * @param userContext The user context (record ID) to pass to the agent
     * @param currentActivity The current activity to present the chat view in (optional, will try to get from context if null)
     * @param callback Callback for presentation result
     */
    fun presentChatView(
        agentId: String,
        userContext: String,
        currentActivity: Activity? = null,
        callback: PresentationCallback
    ) {
        try {
            val client = agentforceClient
                ?: throw IllegalStateException("AgentforceClient not initialized")

            // Use provided activity or fall back to stored context
            val activityContext = currentActivity ?: context
            val coordinator = AgentforceUICoordinator(activityContext)

            // Start conversation with optional agent ID (empty string triggers agent picker)
            val conversation = if (agentId.isNotEmpty()) {
                client.startAgentforceConversation(agentId)
            } else {
                client.startAgentforceConversation()
            }

            // Set record context if userContext is provided
            if (userContext.isNotEmpty()) {
                val contextManager = client.getCurrentContextManager()
                contextManager?.updatePageContext(
                    AgentforcePageContext(
                        recordId = userContext,
                        apiName = "Contact"  // Default to Contact, could be made configurable
                    )
                )
            }

            // Present the chat view by passing a Composable lambda
            coordinator.presentChatView({
                client.AgentforceConversationContainer(
                    conversation = conversation,
                    onClose = {
                        dismissChatView(object : DismissCallback {
                            override fun onSuccess() {
                                // Chat dismissed successfully
                            }

                            override fun onError(error: Exception) {
                                Log.e(TAG, "Error dismissing chat view", error)
                            }
                        })
                    }
                )
            }, callback)

            // Store the coordinator for dismiss operations
            uiCoordinator = coordinator
        } catch (e: Exception) {
            Log.e(TAG, "Error presenting chat view", e)
            callback.onError(e)
        }
    }

    /**
     * Dismisses the currently displayed Agentforce chat view.
     * 
     * @param callback Callback for dismissal result
     */
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
}


