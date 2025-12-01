/*
 * Copyright (c) 2024-present, salesforce.com, inc.
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

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.facebook.react.bridge.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * React Native bridge module for Agentforce Service Agent
 * Provides JavaScript interface for configuring and launching Service Agent conversations
 */
class ServiceAgentModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        private const val TAG = "ServiceAgentModule"
        private const val MODULE_NAME = "AgentforceModule"
    }

    private var viewModel: ServiceAgentViewModel? = null

    override fun getName(): String = MODULE_NAME

    /**
     * Configure Service Agent with required parameters
     * @param serviceApiURL The Salesforce instance URL
     * @param organizationId The Salesforce organization ID
     * @param esDeveloperName The Einstein Service Agent developer name
     * @param promise Promise to resolve with success/failure
     */
    @ReactMethod
    fun configure(
        serviceApiURL: String,
        organizationId: String,
        esDeveloperName: String,
        promise: Promise
    ) {
        Log.d(TAG, "configure() called with URL: $serviceApiURL, OrgId: $organizationId, DevName: $esDeveloperName")

        try {
            val activity = currentActivity
            if (activity == null) {
                Log.e(TAG, "Activity not available")
                promise.reject("ERROR", "Activity not available")
                return
            }

            // Validate inputs
            if (serviceApiURL.isBlank() || organizationId.isBlank() || esDeveloperName.isBlank()) {
                Log.e(TAG, "Invalid configuration parameters")
                promise.reject("ERROR", "All configuration parameters are required")
                return
            }

            // Get or create ViewModel
            if (activity is ViewModelStoreOwner) {
                if (viewModel == null) {
                    viewModel = ViewModelProvider(
                        activity,
                        ViewModelProvider.AndroidViewModelFactory.getInstance(
                            reactApplicationContext.applicationContext as Application
                        )
                    )[ServiceAgentViewModel::class.java]
                }

                // Update configuration
                viewModel?.updateConfiguration(
                    serviceApiURL = serviceApiURL,
                    organizationId = organizationId,
                    esDeveloperName = esDeveloperName
                )

                // Initialize SDK on background thread
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        viewModel?.initializeAgentforce()
                        Log.d(TAG, "Agentforce initialized successfully")
                        promise.resolve(true)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to initialize Agentforce", e)
                        promise.reject("ERROR", "Failed to initialize: ${e.message}")
                    }
                }
            } else {
                Log.e(TAG, "Activity is not ViewModelStoreOwner")
                promise.reject("ERROR", "Activity not compatible")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Configuration error", e)
            promise.reject("ERROR", "Failed to configure: ${e.message}")
        }
    }

    /**
     * Launch the Agentforce conversation UI
     * @param promise Promise to resolve with success/failure
     */
    @ReactMethod
    fun launchConversation(promise: Promise) {
        Log.d(TAG, "launchConversation() called")

        try {
            val activity = currentActivity
            if (activity == null) {
                Log.e(TAG, "Activity not available")
                promise.reject("ERROR", "Activity not available")
                return
            }

            // Check if configured
            if (viewModel?.isConfigured?.value != true) {
                Log.e(TAG, "SDK not configured")
                promise.reject("ERROR", "SDK not configured. Call configure() first.")
                return
            }

            // Launch conversation activity (it will start the conversation itself)
            val intent = Intent(activity, ServiceAgentConversationActivity::class.java)
            activity.startActivity(intent)

            Log.d(TAG, "Conversation activity launched")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch conversation", e)
            promise.reject("ERROR", "Failed to launch: ${e.message}")
        }
    }

    /**
     * Check if SDK is configured and ready
     * @param promise Promise to resolve with boolean status
     */
    @ReactMethod
    fun isConfigured(promise: Promise) {
        try {
            val configured = viewModel?.isConfigured?.value ?: false
            Log.d(TAG, "isConfigured() returning: $configured")
            promise.resolve(configured)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking configuration", e)
            promise.reject("ERROR", e.message)
        }
    }

    override fun invalidate() {
        super.invalidate()
        viewModel = null
    }
}

