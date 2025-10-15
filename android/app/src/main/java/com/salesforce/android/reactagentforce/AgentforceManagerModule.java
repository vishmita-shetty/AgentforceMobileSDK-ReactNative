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
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import androidx.annotation.NonNull;

import android.os.Handler;
import android.os.Looper;

/**
 * React Native bridge module for Agentforce functionality.
 * 
 * This module exposes Agentforce SDK methods to React Native JavaScript,
 * allowing initialization and presentation of the Agentforce chat interface.
 */
public class AgentforceManagerModule extends ReactContextBaseJavaModule {

    private static final String MODULE_NAME = "AgentforceManager";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AgentforceClientManager agentforceClient;
    
    // Cache for configuration to avoid unnecessary re-initialization
    private String cachedAgentId;
    private String cachedOrgId;
    private String cachedEndpoint;

    public AgentforceManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    /**
     * Initializes the Agentforce SDK with configuration parameters.
     * 
     * @param config Configuration map containing:
     *               - agentId: The Agentforce agent identifier
     *               - orgId: The Salesforce organization ID
     *               - endpoint: The Salesforce endpoint URL
     * @param promise Promise to resolve/reject when initialization completes
     */
    @ReactMethod
    public void initializeAgentforce(ReadableMap config, Promise promise) {
        mainHandler.post(() -> {
            try {
                String agentId = config.hasKey("agentId") ? config.getString("agentId") : null;
                String orgId = config.hasKey("orgId") ? config.getString("orgId") : null;
                String endpoint = config.hasKey("endpoint") ? config.getString("endpoint") : null;

                if (agentId == null || orgId == null || endpoint == null) {
                    promise.reject("INVALID_CONFIG", "Missing required configuration parameters");
                    return;
                }

                // Check if configuration has changed
                boolean configChanged = !agentId.equals(cachedAgentId) 
                    || !orgId.equals(cachedOrgId) 
                    || !endpoint.equals(cachedEndpoint);

                // Only create new client if configuration changed or client doesn't exist
                if (agentforceClient == null || configChanged) {
                    // Cache the new configuration
                    cachedAgentId = agentId;
                    cachedOrgId = orgId;
                    cachedEndpoint = endpoint;

                    agentforceClient = new AgentforceClientManager(getReactApplicationContext());
                    agentforceClient.initialize(agentId, orgId, endpoint, getCurrentActivity(), new AgentforceClientManager.InitializationCallback() {
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
     * Presents the Agentforce chat view for a specific agent.
     * 
     * @param agentId The agent identifier to start a conversation with
     * @param promise Promise to resolve/reject when presentation completes
     */
    @ReactMethod
    public void presentAgentforceChatView(String agentId, Promise promise) {
        mainHandler.post(() -> {
            try {
                if (agentforceClient == null) {
                    promise.reject("NOT_INITIALIZED", "AgentforceClient not initialized. Call initializeAgentforce first.");
                    return;
                }

                // Get the current activity from React context
                android.app.Activity currentActivity = getCurrentActivity();
                
                agentforceClient.presentChatView(agentId, currentActivity, new AgentforceClientManager.PresentationCallback() {
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


