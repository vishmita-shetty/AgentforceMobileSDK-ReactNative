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
package com.salesforce.android.reactagentforce;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Handler;
import android.os.Looper;

/**
 * React Native bridge module for Agentforce functionality.
 *
 * This module exposes Agentforce SDK methods to React Native JavaScript,
 * allowing initialization and presentation of the Agentforce chat interface.
 * Also provides event emitter functionality for navigation interception.
 */
public class AgentforceManagerModule extends ReactContextBaseJavaModule {

    private static final String MODULE_NAME = "AgentforceManager";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ReactApplicationContext reactContext;
    private AgentforceClientManager agentforceClient;

    // Cache for configuration to avoid unnecessary re-initialization
    private ReadableArray cachedAgents;
    private String cachedOrgId;
    private String cachedEndpoint;

    public AgentforceManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    /**
     * Sends an event to JavaScript.
     *
     * @param eventName The name of the event
     * @param params The parameters to send with the event
     */
    private void sendEvent(String eventName, @Nullable WritableMap params) {
        if (reactContext.hasActiveCatalystInstance()) {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
        }
    }

    /**
     * Emits a navigation event when Agentforce navigates to a record.
     *
     * @param recordId The ID of the record being navigated to
     * @param recordType The type of the record (e.g., "Contact", "Account")
     */
    public void emitNavigationEvent(String recordId, String recordType) {
        WritableMap params = Arguments.createMap();
        params.putString("id", recordId);
        params.putString("type", recordType);
        sendEvent("agentforceNavigation", params);
    }

    /**
     * Required for RN built-in Event Emitter support.
     * This method is called when JavaScript code adds a listener.
     *
     * @param eventName The name of the event being listened to
     */
    @ReactMethod
    public void addListener(String eventName) {
        // Keep: Required for RN built-in Event Emitter support
        // No implementation needed - DeviceEventManagerModule handles the actual subscription
    }

    /**
     * Required for RN built-in Event Emitter support.
     * This method is called when JavaScript code removes listeners.
     *
     * @param count The number of listeners to remove
     */
    @ReactMethod
    public void removeListeners(Integer count) {
        // Keep: Required for RN built-in Event Emitter support
        // No implementation needed - DeviceEventManagerModule handles the actual cleanup
    }

    /**
     * Initializes the Agentforce SDK with configuration parameters.
     *
     * @param config Configuration map containing:
     *               - agents: Array of agent objects with id, label, and isDefault properties
     *               - orgId: The Salesforce organization ID
     *               - endpoint: The Salesforce endpoint URL
     * @param promise Promise to resolve/reject when initialization completes
     */
    @ReactMethod
    public void initializeAgentforce(ReadableMap config, Promise promise) {
        mainHandler.post(() -> {
            try {
                ReadableArray agents = config.hasKey("agents") ? config.getArray("agents") : null;
                String orgId = config.hasKey("orgId") ? config.getString("orgId") : null;
                String endpoint = config.hasKey("endpoint") ? config.getString("endpoint") : null;

                if (agents == null || agents.size() == 0 || orgId == null || endpoint == null) {
                    promise.reject("INVALID_CONFIG", "Missing required configuration parameters");
                    return;
                }

                // Check if configuration has changed
                boolean configChanged = !areAgentsEqual(agents, cachedAgents)
                    || !orgId.equals(cachedOrgId)
                    || !endpoint.equals(cachedEndpoint);

                // Only create new client if configuration changed or client doesn't exist
                if (agentforceClient == null || configChanged) {
                    // Cache the new configuration
                    cachedAgents = agents;
                    cachedOrgId = orgId;
                    cachedEndpoint = endpoint;

                    agentforceClient = new AgentforceClientManager(getReactApplicationContext(), this);
                    agentforceClient.initialize(agents, orgId, endpoint, getCurrentActivity(), new AgentforceClientManager.InitializationCallback() {
                        @Override
                        public void onSuccess() {
                            WritableMap result = Arguments.createMap();
                            result.putBoolean("success", true);
                            result.putBoolean("reinitialized", configChanged);
                            promise.resolve(result);
                        }

                        @Override
                        public void onError(Exception error) {
                            promise.reject("INIT_ERROR", error.getMessage(), error);
                        }
                    });
                } else {
                    // Configuration hasn't changed, reuse existing client
                    WritableMap result = Arguments.createMap();
                    result.putBoolean("success", true);
                    result.putBoolean("reinitialized", false);
                    promise.resolve(result);
                }
            } catch (Exception e) {
                promise.reject("INIT_EXCEPTION", e.getMessage(), e);
            }
        });
    }

    /**
     * Helper method to compare two agent arrays for equality.
     */
    private boolean areAgentsEqual(ReadableArray agents1, ReadableArray agents2) {
        if (agents1 == agents2) return true;
        if (agents1 == null || agents2 == null) return false;
        if (agents1.size() != agents2.size()) return false;

        for (int i = 0; i < agents1.size(); i++) {
            ReadableMap agent1 = agents1.getMap(i);
            ReadableMap agent2 = agents2.getMap(i);

            String id1 = agent1.getString("id");
            String id2 = agent2.getString("id");

            if (!id1.equals(id2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Presents the Agentforce chat view for a specific agent with optional context.
     *
     * @param agentId The agent identifier to start a conversation with (empty string for agent picker)
     * @param userContext The user context (record ID) to pass to the agent
     * @param promise Promise to resolve/reject when presentation completes
     */
    @ReactMethod
    public void presentAgentforceChatView(String agentId, String userContext, Promise promise) {
        mainHandler.post(() -> {
            try {
                if (agentforceClient == null) {
                    promise.reject("NOT_INITIALIZED", "AgentforceClient not initialized. Call initializeAgentforce first.");
                    return;
                }

                // Get the current activity from React context
                android.app.Activity currentActivity = getCurrentActivity();

                agentforceClient.presentChatView(agentId, userContext, currentActivity, new AgentforceClientManager.PresentationCallback() {
                    @Override
                    public void onSuccess() {
                        WritableMap result = Arguments.createMap();
                        result.putBoolean("success", true);
                        promise.resolve(result);
                    }

                    @Override
                    public void onError(Exception error) {
                        promise.reject("PRESENT_ERROR", error.getMessage(), error);
                    }
                });
            } catch (Exception e) {
                promise.reject("PRESENT_EXCEPTION", e.getMessage(), e);
            }
        });
    }

    /**
     * Dismisses the currently displayed Agentforce chat view.
     * 
     * @param promise Promise to resolve/reject when dismissal completes
     */
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
}


