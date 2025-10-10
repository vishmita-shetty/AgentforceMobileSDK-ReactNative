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
package com.salesforce.agentforceReact
import android.app.Application
import android.content.Context
import android.util.Log
import com.salesforce.android.agentforcesdkimpl.AgentforceClient
import com.salesforce.android.agentforcesdkimpl.AgentforceUIDelegate
import com.salesforce.android.agentforcesdkimpl.configuration.AgentforceConfiguration
import com.salesforce.android.agentforcesdkimpl.configuration.AgentforceMode
import com.salesforce.android.agentforcesdkimpl.utils.AgentforceFeatureFlagSettings
import com.salesforce.android.mobile.interfaces.user.Community
import com.salesforce.android.mobile.interfaces.user.Org
import com.salesforce.android.mobile.interfaces.user.User
import com.salesforce.androidsdk.accounts.UserAccount
import com.salesforce.androidsdk.accounts.UserAccountManager

/**
 * Manager class for Agentforce SDK integration.
 * 
 * This class handles initialization of the Agentforce SDK and presentation
 * of the chat interface within a React Native application.
 */
class AgentforceClientManager(private val context: Context) {

    companion object {
        private const val TAG = "AgentforceClientManager"
    }

    private var agentforceClient: AgentforceClient? = null
    private var uiCoordinator: AgentforceUICoordinator? = null

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
     * @param agentId The Agentforce agent identifier
     * @param orgId The Salesforce organization ID
     * @param endpoint The Salesforce endpoint URL
     * @param callback Callback for initialization result
     */
    fun initialize(
        agentId: String,
        orgId: String,
        endpoint: String,
        callback: InitializationCallback
    ) {
        try {
            // Get current user account
            val userAccountManager = UserAccountManager.getInstance()
            val currentUser: UserAccount = userAccountManager.currentUser
                ?: throw IllegalStateException("No authenticated user found")

            // Create protocol implementations
            val credentialProvider = AgentforceCredentialProvider()
            val networkProvider = SalesforceNetworkProvider(context)
            val logger = SalesforceLoggerService()

            // Create feature flag settings
            val featureFlagSettings = AgentforceFeatureFlagSettings.builder()
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

            // Create Agentforce configuration
            val config = AgentforceConfiguration.builder(credentialProvider)
                .setUser(user)
                .setSalesforceDomain("test.salesforce.com")
                .setAgentId("agent123")
                .setFeatureFlagSettings(featureFlagSettings)
                .setNetwork(networkProvider)
                .setLogger(logger)
                .setSalesforceDomain(instanceUrl)
                .build()

            val agentforceMode = AgentforceMode.FullConfig(config)
            val app = context.applicationContext as Application

            // Create client
            agentforceClient?.init(
                authCredentialProvider = credentialProvider,
                agentforceMode = agentforceMode,
                application = app
            )
            // Initialize UI coordinator
            uiCoordinator = AgentforceUICoordinator(context)

            callback.onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Agentforce", e)
            callback.onError(e)
        }
    }

    /**
     * Presents the Agentforce chat view for the specified agent.
     * 
     * @param agentId The agent identifier to start a conversation with
     * @param callback Callback for presentation result
     */
    fun presentChatView(agentId: String, callback: PresentationCallback) {
        try {
            val client = agentforceClient
                ?: throw IllegalStateException("AgentforceClient not initialized")
            val coordinator = uiCoordinator
                ?: throw IllegalStateException("UI Coordinator not initialized")

            // Start conversation
            val conversation = client.startAgentforceConversation(agentId)

            // Present the chat view by passing a Composable lambda
            coordinator.presentChatView({
                client.AgentforceConversationContainer(
                    conversation = conversation,
                    onClose = {
                        dismissChatView(object : DismissCallback {
                            override fun onSuccess() {
                                Log.d(TAG, "Chat view closed")
                            }

                            override fun onError(error: Exception) {
                                Log.e(TAG, "Error dismissing chat view", error)
                            }
                        })
                    }
                )
            }, callback)
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


