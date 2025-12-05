/*
 * Copyright (c) 2024-present, salesforce.com, inc.
 * All rights reserved.
 */
package com.salesforce.android.reactagentforce

import com.salesforce.android.agentforcesdkimpl.AgentforceClient
import com.salesforce.android.agentforcesdkimpl.AgentforceConversation

/**
 * Singleton holder for AgentforceClient to share across activities
 * This is necessary because ViewModels are activity-scoped
 */
object AgentforceClientHolder {
    
    @Volatile
    var agentforceClient: AgentforceClient? = null
        private set
    
    @Volatile
    var currentConversation: AgentforceConversation? = null
        private set
    
    @Volatile
    var isConfigured: Boolean = false
        private set
    
    fun setClient(client: AgentforceClient) {
        agentforceClient = client
        isConfigured = true
    }
    
    fun setConversation(conversation: AgentforceConversation?) {
        currentConversation = conversation
    }
    
    fun clear() {
        currentConversation = null
        agentforceClient = null
        isConfigured = false
    }
}

